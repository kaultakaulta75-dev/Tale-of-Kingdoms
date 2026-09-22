package com.convallyria.taleofkingdoms.common.kingdom;

import com.convallyria.taleofkingdoms.common.kingdom.builds.BuildCosts;
import com.convallyria.taleofkingdoms.common.kingdom.poi.KingdomPOI;
import com.convallyria.taleofkingdoms.common.serialization.EnumCodec;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerKingdom {

    public static final Codec<PlayerKingdom> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockPos.CODEC.fieldOf("start").forGetter(PlayerKingdom::getStart),
                    BlockPos.CODEC.fieldOf("end").forGetter(PlayerKingdom::getEnd),
                    BlockPos.CODEC.fieldOf("origin").forGetter(PlayerKingdom::getOrigin),
                    Codec.unboundedMap(new EnumCodec<>(KingdomPOI.class), BlockPos.CODEC).fieldOf("poi").forGetter(PlayerKingdom::getPoi),
                    new EnumCodec<>(BuildCosts.class).listOf().fieldOf("built_buildings").forGetter(PlayerKingdom::getBuiltBuildings),
                    new EnumCodec<>(KingdomTier.class).fieldOf("tier").forGetter(PlayerKingdom::getTier),
                    Codec.LONG.fieldOf("last_stock_market_update").forGetter(PlayerKingdom::getLastStockMarketUpdate),
                    Codec.LONG.fieldOf("last_tax_collection").forGetter(PlayerKingdom::getLastTaxCollection)
            ).apply(instance, (start, end, origin, poi, builtBuildings, tier, lastStockMarketUpdate, lastTaxCollection) -> {
                final PlayerKingdom playerKingdom = new PlayerKingdom(origin);
                playerKingdom.setStart(start);
                playerKingdom.setEnd(end);
                poi.forEach(playerKingdom::addPOI);
                builtBuildings.forEach(playerKingdom::addBuilt);
                playerKingdom.setTier(tier);
                playerKingdom.setLastStockMarketUpdate(lastStockMarketUpdate);
                playerKingdom.lastTaxCollection = lastTaxCollection;
                return playerKingdom;
            }
    ));

    private BlockPos start, end;
    private BlockPos origin;
    private final Map<KingdomPOI, BlockPos> poi;
    private final List<BuildCosts> builtBuildings;
    private KingdomTier tier;
    private long lastStockMarketUpdate, lastTaxCollection;
    private transient volatile boolean constructionInProgress;

    public PlayerKingdom(BlockPos origin) {
        this.origin = origin;
        this.poi = new HashMap<>();
        this.builtBuildings = new ArrayList<>();
        this.tier = KingdomTier.TIER_ONE;
    }

    public <T extends Entity> Optional<T> getKingdomEntity(World world, EntityType<T> type) {
        if (start == null || end == null) return Optional.empty();
        Box box = Box.enclosing(start, end);
        return world.getEntitiesByType(type, box, entity -> true).stream().findFirst();
    }

    public KingdomTier getTier() {
        return tier;
    }

    public void setTier(KingdomTier tier) {
        this.tier = tier;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
    }

    public BlockPos getStart() {
        return start;
    }

    public void setStart(BlockPos start) {
        this.start = start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public void setEnd(BlockPos end) {
        this.end = end;
    }

    public Map<KingdomPOI, BlockPos> getPoi() {
        return poi;
    }

    public void addPOI(KingdomPOI poi, BlockPos pos) {
        this.poi.put(poi, pos);
    }

    public BlockPos getPOIPos(KingdomPOI poi) {
        return this.poi.get(poi);
    }

    public List<BuildCosts> getBuiltBuildings() {
        return builtBuildings;
    }

    public boolean hasBuilt(BuildCosts poi) {
        return this.builtBuildings.contains(poi);
    }

    public void addBuilt(BuildCosts poi) {
        if (!this.builtBuildings.contains(poi)) this.builtBuildings.add(poi);
    }

    public boolean hasCompletedTier(KingdomTier checkedTier) {
        for (BuildCosts build : BuildCosts.values()) {
            if (build.getTier() == checkedTier && !hasBuilt(build)) return false;
        }
        return true;
    }

    public synchronized boolean beginConstruction() {
        if (constructionInProgress) return false;
        constructionInProgress = true;
        return true;
    }

    public synchronized void finishConstruction() {
        constructionInProgress = false;
    }

    public boolean isConstructionInProgress() {
        return constructionInProgress;
    }

    public long getLastStockMarketUpdate() {
        return lastStockMarketUpdate;
    }

    public void setLastStockMarketUpdate(long lastStockMarketUpdate) {
        this.lastStockMarketUpdate = lastStockMarketUpdate;
    }

    public long getLastTaxCollection() {
        return lastTaxCollection;
    }

    public int tryTaxCollection(GuildPlayer benefitter) {
        final long currentTime = System.currentTimeMillis();
        final long timeSince = currentTime - lastTaxCollection;
        // If less than an hour, don't tax
        if (timeSince < 3600000) return 0;

        int totalGold = 0;
        for (BuildCosts builtBuilding : builtBuildings) {
            if (builtBuilding == BuildCosts.SMALL_HOUSE_1 || builtBuilding == BuildCosts.SMALL_HOUSE_2) {
                totalGold += 50;
                continue;
            }

            if (builtBuilding == BuildCosts.LARGE_HOUSE) {
                totalGold += 125;
                continue;
            }

            if (builtBuilding == BuildCosts.TIER_2_SMALL_HOUSE_1 || builtBuilding == BuildCosts.TIER_2_SMALL_HOUSE_2) {
                totalGold += 175;
                continue;
            }

            if (builtBuilding == BuildCosts.TIER_2_LARGE_HOUSE) {
                totalGold += 250;
            }
        }

        if (totalGold > 0 && !benefitter.tryCreditCoins(totalGold)) return 0;
        this.lastTaxCollection = currentTime;
        return totalGold;
    }

    public boolean isInKingdom(BlockPos pos) {
        if (start == null || end == null) return false; // Probably still pasting.
        BlockBox blockBox = new BlockBox(end.getX(), end.getY(), end.getZ(), start.getX(), start.getY(), start.getZ());
        return blockBox.contains(pos);
    }

    /**
     * Finds a two-block-high, dry space close to the castle well. This is used
     * after large schematic replacements so the player cannot be trapped in a
     * newly placed wall, floor or roof.
     */
    public Optional<BlockPos> findSafeArrival(World world) {
        BlockPos anchor = getPOIPos(KingdomPOI.CITY_BUILDER_WELL_POI);
        if (anchor == null) anchor = origin;
        if (anchor == null) return Optional.empty();

        int[][] offsets = {
                {2, 0}, {-2, 0}, {0, 2}, {0, -2},
                {3, 2}, {-3, 2}, {3, -2}, {-3, -2},
                {0, 0}
        };
        for (int[] offset : offsets) {
            BlockPos column = anchor.add(offset[0], 0, offset[1]);
            for (int yOffset = -2; yOffset <= 6; yOffset++) {
                BlockPos candidate = column.up(yOffset);
                if (isSafeArrival(world, candidate)) return Optional.of(candidate);
            }
        }

        int surfaceY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, anchor.getX(), anchor.getZ());
        BlockPos surface = new BlockPos(anchor.getX(), surfaceY, anchor.getZ());
        return isSafeArrival(world, surface) ? Optional.of(surface) : Optional.empty();
    }

    private boolean isSafeArrival(World world, BlockPos pos) {
        BlockPos head = pos.up();
        BlockPos floor = pos.down();
        return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(head).getCollisionShape(world, head).isEmpty()
                && !world.getBlockState(floor).getCollisionShape(world, floor).isEmpty()
                && world.getFluidState(pos).isEmpty()
                && world.getFluidState(head).isEmpty();
    }
}
