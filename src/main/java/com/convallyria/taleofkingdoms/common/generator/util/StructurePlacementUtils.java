package com.convallyria.taleofkingdoms.common.generator.util;

import net.minecraft.util.math.random.Random;
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

    private StructurePlacementUtils() {
    }

    public static boolean passesSpawnRate(Random random, int percentage) {
        int clampedPercentage = Math.max(0, Math.min(100, percentage));
        return random.nextInt(100) < clampedPercentage;
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
