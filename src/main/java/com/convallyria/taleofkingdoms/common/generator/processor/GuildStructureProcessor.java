package com.convallyria.taleofkingdoms.common.generator.processor;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class GuildStructureProcessor extends StructureProcessor {

    public static final GuildStructureProcessor INSTANCE = new GuildStructureProcessor();
    public static final MapCodec<GuildStructureProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private final List<SchematicOptions> options;
    private final Set<UUID> assignedEntities = new HashSet<>();
    private final boolean restoreGuildFarms;
    private final List<StructureTemplate.StructureBlockInfo> farmland = new ArrayList<>();
    private final List<StructureTemplate.StructureBlockInfo> crops = new ArrayList<>();

    public GuildStructureProcessor(SchematicOptions... options) {
        this(false, options);
    }

    public GuildStructureProcessor(boolean restoreGuildFarms, SchematicOptions... options) {
        this.restoreGuildFarms = restoreGuildFarms;
        this.options = Arrays.asList(options);
    }

    /**
     * Structure placement can update a crop before all of its supporting blocks
     * have settled. Replant the castle fields after the complete template has
     * been placed, with farmland first and the plants last.
     */
    public void restoreFarms(ServerWorld world) {
        restoreFarms(world, false);
    }

    /** Replant missing crops in saves made before fields were restored after placement. */
    public static void restoreExistingFarms(ServerWorld world, BlockPos origin) {
        StructureTemplate template = world.getStructureTemplateManager().getTemplate(Schematic.GUILD_CASTLE.getPath())
                .orElseThrow(() -> new IllegalStateException("Missing guild castle structure template"));
        StructurePlacementData data = new StructurePlacementData().setRotation(BlockRotation.NONE);
        GuildStructureProcessor processor = new GuildStructureProcessor(true);
        processor.farmland.addAll(template.getInfosForBlock(origin, data, Blocks.FARMLAND, true));
        processor.crops.addAll(template.getInfosForBlock(origin, data, Blocks.WHEAT, true));
        processor.crops.addAll(template.getInfosForBlock(origin, data, Blocks.BEETROOTS, true));
        if (processor.farmland.isEmpty() || processor.crops.isEmpty()) {
            throw new IllegalStateException("Guild castle template contains no farmland or crops");
        }
        processor.restoreFarms(world, true);
    }

    private void restoreFarms(ServerWorld world, boolean onlyMissingCrops) {
        if (!restoreGuildFarms) return;

        Set<BlockPos> plantedPositions = new HashSet<>();
        for (StructureTemplate.StructureBlockInfo crop : crops) plantedPositions.add(crop.pos());

        int restoredFarmland = 0;
        for (StructureTemplate.StructureBlockInfo block : farmland) {
            if (onlyMissingCrops && (!plantedPositions.contains(block.pos().up())
                    || !world.getBlockState(block.pos().up()).isAir()
                    || !world.getBlockState(block.pos()).isOf(Blocks.DIRT))) continue;
            if (!world.getBlockState(block.pos()).isOf(Blocks.FARMLAND)
                    && world.setBlockState(block.pos(), block.state(), Block.NOTIFY_LISTENERS)) {
                restoredFarmland++;
            }
        }

        int restoredCrops = 0;
        int missingFarmland = 0;
        for (StructureTemplate.StructureBlockInfo block : crops) {
            if (onlyMissingCrops && !world.getBlockState(block.pos()).isAir()) continue;
            if (!world.getBlockState(block.pos().down()).isOf(Blocks.FARMLAND)) {
                missingFarmland++;
                continue;
            }
            if (!world.getBlockState(block.pos()).equals(block.state())
                    && world.setBlockState(block.pos(), block.state(), Block.NOTIFY_LISTENERS)) {
                restoredCrops++;
            }
        }

        TaleOfKingdoms.LOGGER.info("Guild fields: restored {} farmland blocks and {} crops ({} without farmland)",
                restoredFarmland, restoredCrops, missingFarmland);
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo currentBlockInfo, StructurePlacementData data) {
        ServerWorldAccess serverWorldAccess = (ServerWorldAccess) world;
        if (restoreGuildFarms) {
            BlockState state = currentBlockInfo.state();
            if (state.isOf(Blocks.FARMLAND)) {
                farmland.add(currentBlockInfo);
            } else if (state.isOf(Blocks.WHEAT) || state.isOf(Blocks.BEETROOTS)) {
                crops.add(currentBlockInfo);
            }
        }
        if (currentBlockInfo.state().getBlock() instanceof StructureBlock) {
            StructureTemplate.StructureBlockInfo air = new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), Blocks.AIR.getDefaultState(), new NbtCompound());
            String metadata = currentBlockInfo.nbt().getString("metadata");
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) return currentBlockInfo;
            Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
            TaleOfKingdoms.LOGGER.debug(currentBlockInfo.pos());
            if (instance.isEmpty()) return currentBlockInfo;

            if (metadata.equalsIgnoreCase("Gateway")) {
                if (!instance.get().isLoaded()) instance.get().getReficuleAttackLocations().add(currentBlockInfo.pos());
                return air;
            }

            if (options.contains(SchematicOptions.NO_ENTITIES)) return air;

            Vec3d spawnPos = currentBlockInfo.pos().toCenterPos();
            @SuppressWarnings("unchecked")
            final EntityType<? extends MobEntity> type = (EntityType<? extends MobEntity>) Registries.ENTITY_TYPE
                    .getOrEmpty(Identifier.of(TaleOfKingdoms.MODID, metadata)).orElse(null);
            if (type == null) {
                TaleOfKingdoms.LOGGER.error("Unable to find entity {}", metadata);
                return air;
            }

            if (options.contains(SchematicOptions.IGNORE_DEFENDERS)
                    && (type.equals(EntityTypes.GUILDGUARD) || type.equals(EntityTypes.GUILDARCHER) || type.equals(EntityTypes.GUILDVILLAGER))) {
                return air;
            }

            BlockPos entityPosition = BlockPos.ofFloored(spawnPos);
            boolean repeatedType = type.equals(EntityTypes.GUILDGUARD)
                    || type.equals(EntityTypes.GUILDARCHER)
                    || type.equals(EntityTypes.GUILDVILLAGER);
            if (!repeatedType) {
                Optional<? extends MobEntity> guildEntity = instance.get().getGuildEntity(serverWorldAccess.toServerWorld(), type);
                if (type.equals(EntityTypes.GUILDMASTER)) {
                    guildEntity = instance.get().getGuildMaster(serverWorldAccess.toServerWorld());
                }

                if (guildEntity.isEmpty()) {
                    EntityUtils.spawnEntity(type, serverWorldAccess, entityPosition);
                } else {
                    guildEntity.get().requestTeleport(spawnPos.x, spawnPos.y, spawnPos.z);
                }
            } else {
                Optional<? extends MobEntity> reusable = instance.get().getGuildEntities(serverWorldAccess.toServerWorld(), type).stream()
                        .filter(entity -> !assignedEntities.contains(entity.getUuid()))
                        .min((first, second) -> Double.compare(
                                first.squaredDistanceTo(spawnPos.x, spawnPos.y, spawnPos.z),
                                second.squaredDistanceTo(spawnPos.x, spawnPos.y, spawnPos.z)));
                if (reusable.isPresent()) {
                    MobEntity entity = reusable.get();
                    assignedEntities.add(entity.getUuid());
                    entity.requestTeleport(spawnPos.x, spawnPos.y, spawnPos.z);
                } else {
                    MobEntity spawned = EntityUtils.spawnEntity(type, serverWorldAccess, entityPosition);
                    if (spawned != null) assignedEntities.add(spawned.getUuid());
                }
            }
            return air;
        }
        return currentBlockInfo;
    }

    protected StructureProcessorType<?> getType() {
        return TaleOfKingdoms.GUILD_PROCESSOR;
    }
}
