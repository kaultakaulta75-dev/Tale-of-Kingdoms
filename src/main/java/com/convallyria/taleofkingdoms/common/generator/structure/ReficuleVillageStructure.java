package com.convallyria.taleofkingdoms.common.generator.structure;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.generator.ReficuleVillageGenerator;
import com.convallyria.taleofkingdoms.common.generator.util.StructurePlacementUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;
import java.util.OptionalInt;

public class ReficuleVillageStructure extends Structure {

    public static final MapCodec<ReficuleVillageStructure> CODEC = createCodec(ReficuleVillageStructure::new);

    public ReficuleVillageStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        if (!StructurePlacementUtils.passesSpawnRate(context.random(), TaleOfKingdoms.CONFIG.mainConfig.reficuleVillageSpawnRate)) {
            return Optional.empty();
        }

        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.x * 16;
        int z = chunkPos.z * 16;
        BlockRotation rotation = BlockRotation.random(context.random());
        int[] footprint = rotatedFootprint(rotation);
        OptionalInt groundHeight = StructurePlacementUtils.findGroundHeight(
                context, x, z, footprint[0], footprint[1], footprint[2], footprint[3], 7);
        if (groundHeight.isEmpty()) return Optional.empty();

        return getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG,
                collector -> this.addPieces(collector, context, groundHeight.getAsInt(), rotation));
    }

    @Override
    public StructureType<?> getType() {
        return TOKStructures.REFICULE_VILLAGE_TYPE;
    }

    public void addPieces(StructurePiecesCollector collector, Context context, int groundHeight, BlockRotation rotation) {
        final ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.x * 16;
        int z = chunkPos.z * 16;
        BlockPos pos = new BlockPos(x, groundHeight, z);
        ReficuleVillageGenerator.addPieces(context.structureTemplateManager(), pos, rotation, collector, context.random());
    }

    private static int[] rotatedFootprint(BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new int[]{-48, 36, 0, 102};
            case CLOCKWISE_180 -> new int[]{-102, 0, -48, 36};
            case COUNTERCLOCKWISE_90 -> new int[]{-36, 48, -102, 0};
            default -> new int[]{0, 102, -36, 48};
        };
    }
}
