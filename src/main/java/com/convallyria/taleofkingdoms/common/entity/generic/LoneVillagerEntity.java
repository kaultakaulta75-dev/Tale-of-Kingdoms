package com.convallyria.taleofkingdoms.common.entity.generic;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.entity.MovementVaried;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.FollowPlayerGoal;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoneVillagerEntity extends TOKEntity implements MovementVaried {

    private static final TrackedData<Boolean> MOVEMENT_ENABLED = DataTracker.registerData(LoneVillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final List<Identifier> VALID_SKINS = List.of(
            identifier("textures/entity/updated_textures/lostvillagerone.png"),
            identifier("textures/entity/updated_textures/lostvillagertwo.png"),
            identifier("textures/entity/updated_textures/lostvillagerthree.png"),
            identifier("textures/entity/updated_textures/lostvillagerfour.png"),
            identifier("textures/entity/updated_textures/lostvillagerfive.png"),
            identifier("textures/entity/updated_textures/lostvillagersix.png"),
            identifier("textures/entity/updated_textures/lostvillagerseven.png"),
            identifier("textures/entity/updated_textures/manone.png"),
            identifier("textures/entity/updated_textures/mantwo.png"),
            identifier("textures/entity/updated_textures/manfive.png")
    );

    private @Nullable UUID rescuerUuid;

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOVEMENT_ENABLED, false);
    }

    public LoneVillagerEntity(@NotNull EntityType<? extends PathAwareEntity> entityType, @NotNull World world) {
        super(entityType, world);
        randomizeSkinVariant(VALID_SKINS.size());
    }

    @Override
    public Optional<Identifier> getSkin() {
        return Optional.of(VALID_SKINS.get(getSkinVariant(VALID_SKINS.size())));
    }

    public boolean isMovementEnabled() {
        return this.dataTracker.get(MOVEMENT_ENABLED);
    }

    public void setMovementEnabled(boolean movementEnabled) {
        this.dataTracker.set(MOVEMENT_ENABLED, movementEnabled);
    }

    @Override
    public void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new FollowPlayerGoal(this, 1.0F, 5, 30,
                player -> rescuerUuid == null || rescuerUuid.equals(player.getUuid())));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return TOKEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0D)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 30.0D);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api != null) {
            Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
            if (instance.isPresent()
                && instance.get().isInGuild(this) && instance.get().getLoneVillagersWithRooms().contains(this.uuid)) {
                if (!player.getWorld().isClient()) Translations.LOST_VILLAGER_GUILD_THANK.send(player);
                return ActionResult.SUCCESS;
            }
        }

        if (player.getWorld().isClient()) return ActionResult.SUCCESS;
        if (rescuerUuid != null && !rescuerUuid.equals(player.getUuid())) {
            player.sendMessage(net.minecraft.text.Text.translatable("message.taleofkingdoms.lost_villager.not_rescuer"), true);
            return ActionResult.SUCCESS;
        }
        if (rescuerUuid == null) rescuerUuid = player.getUuid();
        this.setMovementEnabled(true);
        Translations.LOST_VILLAGER_THANK.send(player);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean isStationary() {
        return !this.isMovementEnabled();
    }

    @Override
    public boolean isPushable() {
        return this.isMovementEnabled();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("MovementEnabled", isMovementEnabled());
        if (rescuerUuid != null) nbt.putUuid("Rescuer", rescuerUuid);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setMovementEnabled(nbt.getBoolean("MovementEnabled"));
        this.rescuerUuid = nbt.containsUuid("Rescuer") ? nbt.getUuid("Rescuer") : null;
    }
}
