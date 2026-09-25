package com.convallyria.taleofkingdoms.common.schematic;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.mixin.PalettedBlockInfoListInvoker;
import com.convallyria.taleofkingdoms.mixin.StructureTemplateAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Places exceptionally large templates over multiple server ticks. */
final class IncrementalStructurePlacer {

    private static final int SLICE_WIDTH = 12;
    private static final int MAX_SLICE_RETRIES = 2;

    private IncrementalStructurePlacer() {
    }

    static CompletableFuture<BlockBox> place(StructureTemplate source,
                                             ServerWorld world,
                                             BlockPos position,
                                             StructurePlacementData placementData,
                                             Random random,
                                             int flags) {
        CompletableFuture<BlockBox> result = new CompletableFuture<>();
        BlockBox completeBox = source.calculateBoundingBox(placementData, position);
        List<StructureTemplate.PalettedBlockInfoList> palettes =
                ((StructureTemplateAccessor) source).taleofkingdoms$getBlockInfoLists();
        if (palettes.isEmpty()) {
            result.completeExceptionally(new IllegalStateException("Large structure contains no block palette"));
            return result;
        }

        int paletteIndex = palettes.size() == 1 ? 0 : random.nextInt(palettes.size());
        List<StructureTemplate.StructureBlockInfo> infos = palettes.get(paletteIndex).getAll();
        Vec3i size = source.getSize();

        // Splitting millions of entries is CPU work only and must not stall a
        // gameplay tick. No world state is accessed from this worker.
        CompletableFuture.supplyAsync(() -> splitAndValidate(infos, size)).whenComplete((slices, splitError) -> {
            if (splitError != null) {
                result.completeExceptionally(splitError);
                return;
            }
            TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) {
                result.completeExceptionally(new IllegalStateException("Tale of Kingdoms API stopped during structure preparation"));
                return;
            }
            TaleOfKingdoms.LOGGER.info("Prepared incremental structure placement: {} blocks in {} slices",
                    infos.size(), slices.size());
            api.getScheduler().queue(new PlacementTask(
                    source, world, position, placementData, random, flags, completeBox, slices, result
            ), 0);
        });
        return result;
    }

    private static List<List<StructureTemplate.StructureBlockInfo>> splitAndValidate(
            List<StructureTemplate.StructureBlockInfo> infos, Vec3i size) {
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            throw new IllegalArgumentException("Invalid large structure size: " + size);
        }
        int slicesX = (size.getX() + SLICE_WIDTH - 1) / SLICE_WIDTH;
        int slicesZ = (size.getZ() + SLICE_WIDTH - 1) / SLICE_WIDTH;
        int sliceCount = slicesX * slicesZ;
        int expectedPerSlice = Math.max(16, (infos.size() / sliceCount) + 1);
        List<List<StructureTemplate.StructureBlockInfo>> slices = new ArrayList<>(sliceCount);
        for (int index = 0; index < sliceCount; index++) {
            slices.add(new ArrayList<>(expectedPerSlice));
        }

        for (StructureTemplate.StructureBlockInfo info : infos) {
            BlockPos relative = info.pos();
            if (relative.getX() < 0 || relative.getX() >= size.getX()
                    || relative.getY() < 0 || relative.getY() >= size.getY()
                    || relative.getZ() < 0 || relative.getZ() >= size.getZ()) {
                throw new IllegalStateException("Structure block outside declared bounds: " + relative);
            }
            int sliceX = relative.getX() / SLICE_WIDTH;
            int sliceZ = relative.getZ() / SLICE_WIDTH;
            slices.get(sliceZ * slicesX + sliceX).add(info);
        }
        slices.removeIf(List::isEmpty);
        if (slices.isEmpty()) throw new IllegalStateException("Large structure contains no blocks");
        return slices;
    }

    private static StructureTemplate makeSlice(StructureTemplate source,
                                               List<StructureTemplate.StructureBlockInfo> infos,
                                               boolean includeEntities) {
        StructureTemplate slice = new StructureTemplate();
        slice.setAuthor(source.getAuthor());
        StructureTemplateAccessor accessor = (StructureTemplateAccessor) slice;
        accessor.taleofkingdoms$setSize(source.getSize());
        accessor.taleofkingdoms$setBlockInfoLists(List.of(
                PalettedBlockInfoListInvoker.taleofkingdoms$create(infos)
        ));
        accessor.taleofkingdoms$setEntities(includeEntities
                ? ((StructureTemplateAccessor) source).taleofkingdoms$getEntities()
                : List.of());
        return slice;
    }

    private static final class PlacementTask implements Consumer<MinecraftServer> {
        private final StructureTemplate source;
        private final ServerWorld world;
        private final BlockPos position;
        private final StructurePlacementData placementData;
        private final Random random;
        private final int flags;
        private final BlockBox completeBox;
        private final List<List<StructureTemplate.StructureBlockInfo>> slices;
        private final CompletableFuture<BlockBox> result;
        private int sliceIndex;
        private int retries;

        private PlacementTask(StructureTemplate source,
                              ServerWorld world,
                              BlockPos position,
                              StructurePlacementData placementData,
                              Random random,
                              int flags,
                              BlockBox completeBox,
                              List<List<StructureTemplate.StructureBlockInfo>> slices,
                              CompletableFuture<BlockBox> result) {
            this.source = source;
            this.world = world;
            this.position = position;
            this.placementData = placementData;
            this.random = random;
            this.flags = flags;
            this.completeBox = completeBox;
            this.slices = slices;
            this.result = result;
        }

        @Override
        public void accept(MinecraftServer server) {
            if (result.isDone()) return;
            try {
                StructureTemplate slice = makeSlice(
                        source,
                        slices.get(sliceIndex),
                        sliceIndex == slices.size() - 1
                );
                if (!slice.place(world, position, position, placementData, random, flags)) {
                    throw new IllegalStateException("Minecraft rejected structure slice " + sliceIndex);
                }
                sliceIndex++;
                retries = 0;
                if (sliceIndex >= slices.size()) {
                    TaleOfKingdoms.LOGGER.info("Incremental structure placement completed in {} slices", slices.size());
                    result.complete(completeBox);
                    return;
                }
                if (sliceIndex % Math.max(1, slices.size() / 10) == 0) {
                    TaleOfKingdoms.LOGGER.info("Incremental structure placement: {}%",
                            Math.round(sliceIndex * 100.0F / slices.size()));
                }
            } catch (Throwable error) {
                if (retries++ >= MAX_SLICE_RETRIES) {
                    result.completeExceptionally(new IllegalStateException(
                            "Unable to place structure slice " + sliceIndex + " after retries", error));
                    return;
                }
                TaleOfKingdoms.LOGGER.warn("Retrying structure slice {} ({}/{})",
                        sliceIndex, retries, MAX_SLICE_RETRIES, error);
            }

            TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) {
                result.completeExceptionally(new IllegalStateException("Tale of Kingdoms API stopped during structure placement"));
                return;
            }
            api.getScheduler().queue(this, 0);
        }
    }
}
