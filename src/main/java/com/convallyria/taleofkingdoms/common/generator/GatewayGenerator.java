package com.convallyria.taleofkingdoms.common.generator;

import com.convallyria.taleofkingdoms.common.generator.structure.TOKStructures;
import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public class GatewayGenerator {

    private static final Identifier GATEWAY = Identifier.of(TaleOfKingdoms.MODID, "gateway/gateway");
    private static final Identifier BARS = Identifier.of(TaleOfKingdoms.MODID, "gateway/bars");

    public static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation blockRotation, StructurePiecesHolder structurePiecesHolder, Random random) {
        GatewayPiece gateway = new GatewayPiece(manager, GATEWAY, pos.subtract(new Vec3i(0, 1, 0)), blockRotation, 0);
        structurePiecesHolder.addPiece(gateway);
        BlockPos startPos = offset(pos, 7, 5, blockRotation).subtract(new Vec3i(0, 2, 0));
        int times = Math.max(0, pos.getY() - 1);
        for (int i = 0; i < times; i++) {
            GatewayPiece bars = new GatewayPiece(manager, BARS, startPos, blockRotation, 0);
            structurePiecesHolder.addPiece(bars);
            startPos = startPos.subtract(new Vec3i(0, 1, 0));
        }
    }

    private static BlockPos offset(BlockPos origin, int x, int z, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> origin.add(-z, 0, x);
            case CLOCKWISE_180 -> origin.add(-x, 0, -z);
            case COUNTERCLOCKWISE_90 -> origin.add(z, 0, -x);
            default -> origin.add(x, 0, z);
        };
    }

    public static class GatewayPiece extends SimpleStructurePiece {

        public GatewayPiece(StructureTemplateManager structureManager, Identifier identifier, BlockPos blockPos, BlockRotation blockRotation, int i) {
            super(TOKStructures.GATEWAY, 0, structureManager, identifier, identifier.toString(), createPlacementData(blockRotation), blockPos);
        }

        public GatewayPiece(StructureTemplateManager structureManager, NbtCompound nbtCompound) {
            super(TOKStructures.GATEWAY, nbtCompound, structureManager, (identifier) -> createPlacementData(null));
        }

        public GatewayPiece(StructureContext structureContext, NbtCompound nbtCompound) {
            super(TOKStructures.GATEWAY, nbtCompound, structureContext.structureTemplateManager(), (identifier) -> createPlacementData(null));
        }

        private static StructurePlacementData createPlacementData(@Nullable BlockRotation rotation) {
            return (new StructurePlacementData())
                    .setRotation(rotation != null ? rotation : BlockRotation.random(Random.create()))
                    .setMirror(BlockMirror.NONE)
                    .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos blockPos, ServerWorldAccess serverWorldAccess,
                                      Random random, BlockBox blockBox) {
            switch (metadata) {
                case "ReficuleSoldier" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_SOLDIER, serverWorldAccess, blockPos);
                case "ReficuleArcher" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_GUARDIAN, serverWorldAccess, blockPos);
                case "ReficuleMage" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_MAGE, serverWorldAccess, blockPos);
            }
        }
    }
}
