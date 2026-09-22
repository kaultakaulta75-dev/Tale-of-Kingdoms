package com.convallyria.taleofkingdoms.common.entity.kingdom;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.MultiSkinned;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HumanFarmerEntity extends VillagerEntity implements MultiSkinned {

    private static final TrackedData<Integer> SKIN_VARIANT = DataTracker.registerData(HumanFarmerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final List<Identifier> VALID_SKINS = List.of(
            Identifier.of(TaleOfKingdoms.MODID, "textures/entity/updated_textures/tok_farmer.png"),
            Identifier.of(TaleOfKingdoms.MODID, "textures/entity/updated_textures/innkeeper.png"),
            Identifier.of(TaleOfKingdoms.MODID, "textures/entity/updated_textures/tok_farmer_3.png")
    );

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SKIN_VARIANT, 0);
    }

    public HumanFarmerEntity(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
        this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.FARMER));
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_HOE));
        if (!world.isClient()) this.dataTracker.set(SKIN_VARIANT, this.random.nextInt(VALID_SKINS.size()));
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.FARMER));
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_HOE));
        return data;
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (sound.getId().getPath().toLowerCase(Locale.ROOT).contains("villager")) return;
        super.playSound(sound, volume, pitch);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        return ActionResult.PASS;
    }

    @Override
    public boolean wantsToStartBreeding() {
        return false;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickHandSwing();
    }

    @Override
    public Optional<Identifier> getSkin() {
        return Optional.of(VALID_SKINS.get(Math.floorMod(this.dataTracker.get(SKIN_VARIANT), VALID_SKINS.size())));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("TokSkinVariant", this.dataTracker.get(SKIN_VARIANT));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TokSkinVariant", NbtElement.INT_TYPE)) {
            this.dataTracker.set(SKIN_VARIANT, nbt.getInt("TokSkinVariant"));
        }
    }
}
