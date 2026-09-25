package com.convallyria.taleofkingdoms.common.entity.guild;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.FollowPlayerGoal;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.HealPlayerGoal;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.ImprovedFollowTargetGoal;
import com.convallyria.taleofkingdoms.common.utils.InventoryUtils;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.common.world.guild.GuildQuestProgression;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Set;

public class GuildMasterDefenderEntity extends GuildMasterEntity {
    private boolean givenSword;

    public GuildMasterDefenderEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(1000.0D);
        this.goalSelector.add(1, new MeleeAttackGoal(this, 0.6D, false));
        this.goalSelector.add(2, new FollowPlayerGoal(this, 0.8F, 5F, 15F));
        this.goalSelector.add(3, new HealPlayerGoal(this, 10F));
        this.targetSelector.add(1, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_SOLDIER, false));
        this.targetSelector.add(2, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_GUARDIAN, false));
        this.targetSelector.add(3, new ImprovedFollowTargetGoal<>(this, EntityTypes.REFICULE_MAGE, false));
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, MobEntity.class, 100,
                true, true, livingEntity -> livingEntity instanceof Monster));
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
    }

    @Override
    public boolean isFireImmune() {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api != null) {
            Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
            if (instance.isPresent()) {
                return instance.get().isUnderAttack();
            }
        }
        return false;
    }

    @Override
    public boolean damage(DamageSource damageSource, float f) {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api != null) {
            if (api.getConquestInstanceStorage().mostRecentInstance().isPresent()) {
                ConquestInstance instance = api.getConquestInstanceStorage().mostRecentInstance().get();
                if (instance.isUnderAttack()) {
                    return false;
                }
            }
        }
        return super.damage(damageSource, f);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        if (player.getWorld().isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity)) return ActionResult.FAIL;
        TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null) return ActionResult.FAIL;
        Optional<ConquestInstance> optionalInstance = api.getConquestInstanceStorage().mostRecentInstance();
        if (optionalInstance.isEmpty()) return ActionResult.FAIL;
        ConquestInstance instance = optionalInstance.get();
        if (instance.isUnderAttack()) {
            int activeAttackers = instance.reconcileReficuleAttackers(serverPlayerEntity.getServerWorld());
            Set<Item> swords = Set.of(Items.IRON_SWORD, Items.STONE_SWORD, Items.DIAMOND_SWORD, Items.GOLDEN_SWORD, Items.WOODEN_SWORD, Items.NETHERITE_SWORD);
            if (!givenSword && !player.getInventory().containsAny(swords)) {
                ItemStack sword = new ItemStack(Items.IRON_SWORD);
                serverPlayerEntity.getInventory().insertStack(sword);
                if (!sword.isEmpty()) serverPlayerEntity.dropItem(sword, false);
                this.givenSword = true;
                return ActionResult.SUCCESS;
            }

            if (activeAttackers == 0) {
                final GuildPlayer guildPlayer = instance.getPlayer(player);
                if (guildPlayer != null && !guildPlayer.hasRebuiltGuild()
                        && guildPlayer.getWorthiness() >= GuildQuestProgression.DEFEND_GUILD_WORTHINESS
                        && guildPlayer.getKingdom() == null) {
                    PlayerInventory playerInventory = serverPlayerEntity.getInventory();
                    if (InventoryUtils.count(playerInventory, ItemTags.LOGS) >= 64) {
                        if (!instance.beginGuildRebuild()) return ActionResult.SUCCESS;
                        if (!InventoryUtils.remove(playerInventory, ItemTags.LOGS, 64)) {
                            instance.finishGuildRebuild();
                            return ActionResult.SUCCESS;
                        }
                        instance.rebuild(serverPlayerEntity, api).whenComplete((box, error) -> {
                            instance.finishGuildRebuild();
                            if (error != null) {
                                ItemStack refund = new ItemStack(Items.OAK_LOG, 64);
                                playerInventory.insertStack(refund);
                                if (!refund.isEmpty()) serverPlayerEntity.dropItem(refund, false);
                                serverPlayerEntity.sendMessage(Text.translatable("message.taleofkingdoms.guild.rebuild_failed"), false);
                                TaleOfKingdoms.LOGGER.error("Guild defender rebuild failed for {}", serverPlayerEntity.getName().getString(), error);
                                return;
                            }
                            guildPlayer.setHasRebuiltGuild(true);
                            instance.setUnderAttack(false);
                            final Entity entity = serverPlayerEntity.getWorld().getEntityById(this.getId());
                            if (entity != null) entity.remove(Entity.RemovalReason.DISCARDED);
                            Translations.GUILDMASTER_THANK_YOU.send(serverPlayerEntity);
                            serverPlayerEntity.sendMessage(GuildQuestProgression.getCurrentObjective(instance, guildPlayer), false);
                        });
                    } else {
                        Translations.GUILDMASTER_REBUILD.send(serverPlayerEntity);
                    }
                    return ActionResult.SUCCESS;
                }
            } else if (activeAttackers <= 4) {
                Translations.GUILDMASTER_KILL_REFICULES.send(player);
            } else {
                Translations.GUILDMASTER_STAY_CLOSE.send(player);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putBoolean("givenSword", givenSword);
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        this.givenSword = tag.getBoolean("givenSword");
        super.readNbt(tag);
    }
}
