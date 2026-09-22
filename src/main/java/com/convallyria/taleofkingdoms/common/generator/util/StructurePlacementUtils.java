package com.convallyria.taleofkingdoms.common.generator.util;

import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;

import java.util.Arrays;
import java.util.OptionalInt;

/**
 * Shared placement checks for Tale of Kingdoms world-generation structures.
 * Keeping these checks deterministic is important: the same world seed must
 * always produce the same structures after a reload or a chunk regeneration.
 */
public final class StructurePlacementUtils {

    public enum FoundationProblem {
        NONE(null),
        OUTSIDE_WORLD_BORDER("message.taleofkingdoms.kingdom.site_world_border"),
        TOO_STEEP("message.taleofkingdoms.kingdom.site_too_steep"),
        TOO_MUCH_WATER("message.taleofkingdoms.kingdom.site_too_wet"),
        TEMPLATE_UNAVAILABLE("message.taleofkingdoms.kingdom.site_unavailable");

        private final String translationKey;

        FoundationProblem(String translationKey) {
            this.translationKey = translationKey;
        }

        public boolean isSuitable() {
            return this == NONE;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    private StructurePlacementUtils() {
    }

    public static boolean passesSpawnRate(Random random, int percentage) {
        int clampedPercentage = Math.max(0, Math.min(100, percentage));
        return random.nextInt(100) < clampedPercentage;
    }

    /**
     * Validates the whole footprint of a manually placed castle before blocks
     * are changed. A few wet samples are accepted so a small pond or stream at
     * the edge does not reject an otherwise good location.
     */
    public static FoundationProblem inspectFoundation(ServerWorld world,
                                                       BlockPos origin,
                                                       int width,
                                                       int depth,
                                                       int maxHeightDifference) {
        if (width <= 0 || depth <= 0) return FoundationProblem.TEMPLATE_UNAVAILABLE;

        BlockPos farCorner = origin.add(width - 1, 0, depth - 1);
        if (!world.getWorldBorder().contains(origin) || !world.getWorldBorder().contains(farCorner)) {
            return FoundationProblem.OUTSIDE_WORLD_BORDER;
        }

        final int samplesPerAxis = 5;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        int wetSamples = 0;

        for (int sampleX = 0; sampleX < samplesPerAxis; sampleX++) {
            int x = origin.getX() + interpolate(0, width - 1, sampleX, samplesPerAxis);
            for (int sampleZ = 0; sampleZ < samplesPerAxis; sampleZ++) {
                int z = origin.getZ() + interpolate(0, depth - 1, sampleZ, samplesPerAxis);
                int groundY = world.getTopY(Heightmap.Type.OCEAN_FLOOR, x, z);
                minHeight = Math.min(minHeight, groundY);
                maxHeight = Math.max(maxHeight, groundY);

                // OCEAN_FLOOR returns the first block above solid ground. A
                // fluid there means that this part of the footprint is under
                // water or lava.
                if (!world.getFluidState(new BlockPos(x, groundY, z)).isEmpty()) {
                    wetSamples++;
                }
            }
        }

        if (maxHeight - minHeight > maxHeightDifference) {
            return FoundationProblem.TOO_STEEP;
        }
        if (wetSamples > 4) {
            return FoundationProblem.TOO_MUCH_WATER;
        }
        return FoundationProblem.NONE;
    }

    /**
     * Samples the complete footprint instead of only its origin. The median
     * ground height gives terrain adaptation a stable level to work from, while
     * the maximum variation prevents large buildings from crossing cliffs.
     */
    public static OptionalInt findGroundHeight(Structure.Context context,
                                                int originX,
                                                int originZ,
                                                int minOffsetX,
                                                int maxOffsetX,
                                                int minOffsetZ,
                                                int maxOffsetZ,
                                                int maxHeightDifference) {
        int footprintWidth = maxOffsetX - minOffsetX;
        int footprintDepth = maxOffsetZ - minOffsetZ;
        int samplesPerAxis = Math.max(footprintWidth, footprintDepth) >= 48 ? 5 : 3;
        int[] heights = new int[samplesPerAxis * samplesPerAxis];
        int index = 0;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        for (int sampleX = 0; sampleX < samplesPerAxis; sampleX++) {
            int x = originX + interpolate(minOffsetX, maxOffsetX, sampleX, samplesPerAxis);
            for (int sampleZ = 0; sampleZ < samplesPerAxis; sampleZ++) {
                int z = originZ + interpolate(minOffsetZ, maxOffsetZ, sampleZ, samplesPerAxis);
                int height = context.chunkGenerator().getHeightInGround(
                        x,
                        z,
                        Heightmap.Type.OCEAN_FLOOR_WG,
                        context.world(),
                        context.noiseConfig()
                );
                heights[index++] = height;
                minHeight = Math.min(minHeight, height);
                maxHeight = Math.max(maxHeight, height);
            }
        }

        if (maxHeight - minHeight > maxHeightDifference) {
            return OptionalInt.empty();
        }

        Arrays.sort(heights);
        return OptionalInt.of(heights[heights.length / 2]);
    }

    private static int interpolate(int min, int max, int sample, int sampleCount) {
        if (sampleCount <= 1) return min;
        return min + Math.round((max - min) * (sample / (float) (sampleCount - 1)));
    }
}
