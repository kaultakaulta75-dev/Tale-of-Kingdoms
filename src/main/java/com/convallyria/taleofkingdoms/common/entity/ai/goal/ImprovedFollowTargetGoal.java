package com.convallyria.taleofkingdoms.common.entity.ai.goal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ImprovedFollowTargetGoal<T extends LivingEntity> extends TrackTargetGoal {
    protected final EntityType<T> entityType;
    protected final int reciprocalChance;
    protected LivingEntity targetEntity;
    private final @Nullable Predicate<LivingEntity> extraPredicate;

    public ImprovedFollowTargetGoal(MobEntity mob, EntityType<T> entityType, boolean checkVisibility) {
        this(mob, entityType, checkVisibility, false);
    }

    public ImprovedFollowTargetGoal(MobEntity mob, EntityType<T> entityType, boolean checkVisibility, boolean checkCanNavigate) {
        this(mob, entityType, 10, checkVisibility, checkCanNavigate, null);
    }

    public ImprovedFollowTargetGoal(MobEntity mob, EntityType<T> entityType, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, checkVisibility, checkCanNavigate);
        this.entityType = entityType;
        this.reciprocalChance = reciprocalChance;
        this.setControls(EnumSet.of(Control.TARGET));
        this.extraPredicate = targetPredicate;
    }

    @Override
    public boolean canStart() {
        if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
        } else {
            this.findClosestTarget();
            return this.targetEntity != null;
        }
    }

    protected Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0D, distance);
    }

    protected void findClosestTarget() {
        Box box = this.getSearchBox(this.getFollowRange());
        List<T> entities = this.mob.getWorld().getEntitiesByType(entityType, box, entity -> {
            if (!entity.isAlive()) return false;
            if (entity instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) return false;
            if (extraPredicate != null && !extraPredicate.test(entity)) return false;
            return !checkVisibility || mob.canSee(entity);
        });
        LivingEntity current = null;
        for (T entity : entities) {
            if (current == null || entity.squaredDistanceTo(mob) < current.squaredDistanceTo(mob)) {
                current = entity;
            }
        }
        this.targetEntity = current;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }

    public void setTargetEntity(@Nullable LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }
}
