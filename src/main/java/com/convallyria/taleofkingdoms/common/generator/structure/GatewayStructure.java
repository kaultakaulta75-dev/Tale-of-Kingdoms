package com.convallyria.taleofkingdoms.common.generator.structure;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.generator.GatewayGenerator;
import com.convallyria.taleofkingdoms.common.generator.util.StructurePlacementUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;
import java.util.OptionalInt;

public class GatewayStructure extends Structure {

    public static final MapCodec<GatewayStructure> CODEC = createCodec(GatewayStructure::new);

    public GatewayStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        if (!StructurePlacementUtils.passesSpawnRate(context.random(), TaleOfKingdoms.CONFIG.mainConfig.gateWaySpawnRate)) {
            return Optional.empty();
        }

        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getCenterX();
        int z = chunkPos.getCenterZ();
        BlockRotation rotation = BlockRotation.random(context.random());
        OptionalInt groundHeight = StructurePlacementUtils.findGroundHeight(context, x, z, -9, 9, -9, 9, 4);
        if (groundHeight.isEmpty()) return Optional.empty();

        return getStructurePosition(context, Heightmap.Type.OCEAN_FLOOR_WG,
                collector -> this.addPieces(collector, context, groundHeight.getAsInt(), rotation));
    }

    @Override
    public StructureType<?> getType() {
        return TOKStructures.GATEWAY_TYPE;
    }

    private void addPieces(StructurePiecesCollector collector, Context context, int groundHeight, BlockRotation blockRotation) {
        final ChunkPos chunkPos = context.chunkPos();
        final ChunkRandom chunkRandom = context.random();
        int x = chunkPos.getCenterX();
        int z = chunkPos.getCenterZ();
        BlockPos blockPos = new BlockPos(x, groundHeight, z);
        GatewayGenerator.addPieces(context.structureTemplateManager(), blockPos, blockRotation, collector, chunkRandom);
    }
}
