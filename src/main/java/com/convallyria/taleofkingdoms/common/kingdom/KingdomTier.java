package com.convallyria.taleofkingdoms.common.kingdom;

import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.world.guild.GuildQuestProgression;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum KingdomTier {
    TIER_ONE(Text.translatable("menu.taleofkingdoms.generic.tier_one"), Schematic.TIER_1_KINGDOM, Vec3i.ZERO, GuildQuestProgression.FOUND_KINGDOM_WORTHINESS),
    TIER_TWO(Text.translatable("menu.taleofkingdoms.generic.tier_two"), Schematic.TIER_2_KINGDOM, new Vec3i(16, 0, 49), GuildQuestProgression.TIER_TWO_WORTHINESS);

    private final Text name;
    private final Schematic schematic;
    private final Vec3i offset;
    private final int requiredWorthiness;

    KingdomTier(Text name, Schematic schematic, Vec3i offset, int requiredWorthiness) {
        this.name = name;
        this.schematic = schematic;
        this.offset = offset;
        this.requiredWorthiness = requiredWorthiness;
    }

    public Text getName() {
        return name;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public Vec3i getOffset() {
        return offset;
    }

    public int getRequiredWorthiness() {
        return requiredWorthiness;
    }

    public Optional<KingdomTier> next() {
        int nextOrdinal = ordinal() + 1;
        return nextOrdinal < values().length ? Optional.of(values()[nextOrdinal]) : Optional.empty();
    }

    public boolean isMaximum() {
        return next().isEmpty();
    }

    public boolean isLowerThanOrEqual(@Nullable KingdomTier tier) {
        if (tier == null) return false;
        return this.ordinal() <= tier.ordinal();
    }

    public boolean isHigherThanOrEqual(@Nullable KingdomTier tier) {
        if (tier == null) return false;
        return this.ordinal() >= tier.ordinal();
    }
}
