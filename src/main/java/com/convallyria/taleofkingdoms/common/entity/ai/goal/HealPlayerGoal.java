package com.convallyria.taleofkingdoms.common.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.EnumSet;

public class HealPlayerGoal extends Goal {

    private final MobEntity mob;
    private LivingEntity target;
    private final float maxDistance;
    private int cooldown;

    public HealPlayerGoal(MobEntity mob, float maxDistance) {
        this.mob = mob;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        this.target = null;
        List<PlayerEntity> list = this.mob.getWorld().getEntitiesByClass(
                PlayerEntity.class,
                this.mob.getBoundingBox().expand(this.maxDistance),
                player -> player.isAlive() && !player.isSpectator() && player.getHealth() < player.getMaxHealth()
        );
        double closestDistance = Double.MAX_VALUE;
        for (PlayerEntity player : list) {
            if (player.isInvisible()) continue;
            double distance = this.mob.squaredDistanceTo(player);
            if (distance >= closestDistance) continue;
            this.target = player;
            closestDistance = distance;
        }
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.target != null
                && this.target.isAlive()
                && this.target.getHealth() < this.target.getMaxHealth()
                && this.mob.squaredDistanceTo(this.target) < (double)(this.maxDistance * this.maxDistance);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0);
            target.addStatusEffect(statusEffectInstance);
            cooldown = 200;
            target = null;
        }
    }
}
