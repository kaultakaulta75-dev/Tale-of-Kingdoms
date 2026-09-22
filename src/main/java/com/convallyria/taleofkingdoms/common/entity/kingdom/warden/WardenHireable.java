package com.convallyria.taleofkingdoms.common.entity.kingdom.warden;

import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.FollowPlayerGoal;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.ImprovedFollowTargetGoal;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.WanderAroundGuildGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public abstract class WardenHireable extends TOKEntity {

    private int internalTotalExperience;

    private static final TrackedData<Integer> TOTAL_EXPERIENCE;

    static {
        TOTAL_EXPERIENCE = DataTracker.registerData(WardenHireable.class, TrackedDataHandlerRegistry.INTEGER);
    }

    public WardenHireable(@NotNull EntityType<? extends PathAwareEntity> entityType, @NotNull World world) {
        super(entityType, world);
    }

    public int getExperiencePoints() {
        return this.dataTracker.get(TOTAL_EXPERIENCE);
    }

    @Override
    public abstract Optional<Identifier> getSkin();

    private Goal followGoal;
    private @Nullable UUID ownerUuid;

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        if (this.getWorld().isClient()) return ActionResult.SUCCESS;
        if (!isOwnedBy(player) && ownerUuid != null) {
            player.sendMessage(Text.translatable("message.taleofkingdoms.soldier.not_owner"), true);
            return ActionResult.SUCCESS;
        }
        this.toggleFollowGoal(player);
        return ActionResult.SUCCESS;
    }

    public void toggleFollowGoal(PlayerEntity cause) {
        if (ownerUuid == null) ownerUuid = cause.getUuid();
        if (!isOwnedBy(cause)) return;
        if (followGoal != null) {
            if (!this.getWorld().isClient()) cause.sendMessage(getGuardText());
            setFollowing(false);
        } else {
            if (!this.getWorld().isClient()) cause.sendMessage(getFollowText());
            setFollowing(true);
        }
    }

    private void setFollowing(boolean following) {
        if (!following) {
            if (followGoal != null) this.goalSelector.remove(followGoal);
            this.followGoal = null;
            return;
        }
        if (followGoal != null || ownerUuid == null) return;
        this.goalSelector.add(5, followGoal = new FollowPlayerGoal(
                this, 0.6D, 2.5f, 30,
                player -> ownerUuid != null && ownerUuid.equals(player.getUuid())
        ));
    }

    public void setOwner(PlayerEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    public boolean isOwnedBy(PlayerEntity player) {
        return ownerUuid != null && ownerUuid.equals(player.getUuid());
    }

    public @Nullable UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean isFollowingPlayer() {
        return followGoal != null;
    }

    public abstract Text getFollowText();

    public abstract Text getGuardText();

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TOTAL_EXPERIENCE, internalTotalExperience);
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other) {
        this.internalTotalExperience++;
        this.dataTracker.set(TOTAL_EXPERIENCE, this.internalTotalExperience);
        if (this.tryLevelUp()) {
            this.updateLevelledAttributes();
            this.playLevelUpEffects();
            this.setHealth(this.getMaxHealth());
        }
        return super.onKilledOther(world, other);
    }

    protected abstract boolean tryLevelUp();

    protected void playLevelUpEffects() {
        for (int i = 0; i < 5; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            this.getWorld().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), d, e, f);
        }
        this.getWorld().playSoundFromEntity(this, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new WanderAroundGuildGoal(this, 0.6D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 30.0F));
        this.targetSelector.add(1, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_SOLDIER, false));
        this.targetSelector.add(2, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_GUARDIAN, false));
        this.targetSelector.add(3, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_MAGE, false));
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, MobEntity.class, 100,
                true, true, livingEntity -> livingEntity instanceof Monster));
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return TOKEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0D)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isStationary() {
        return false;
    }

    public abstract void updateLevelledAttributes();

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.internalTotalExperience = nbt.getInt("XpTotal");
        this.ownerUuid = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
        setFollowing(nbt.getBoolean("Following"));
        this.dataTracker.set(TOTAL_EXPERIENCE, internalTotalExperience);
        this.updateLevelledAttributes();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("XpTotal", internalTotalExperience);
        if (ownerUuid != null) nbt.putUuid("Owner", ownerUuid);
        nbt.putBoolean("Following", followGoal != null);
    }
}
