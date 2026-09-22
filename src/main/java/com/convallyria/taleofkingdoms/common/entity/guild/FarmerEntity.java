package com.convallyria.taleofkingdoms.common.entity.guild;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class FarmerEntity extends TOKEntity {

    public FarmerEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_HOE));
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F, 100F));
    }

    @Override
    public boolean isStationary() {
        return true;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        if (player.getWorld().isClient) return ActionResult.SUCCESS;

        // Check if there is at least 1 Minecraft day difference
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null) return ActionResult.FAIL;
        if (api.getConquestInstanceStorage().mostRecentInstance().isEmpty()) return ActionResult.FAIL;

        ConquestInstance instance = api.getConquestInstanceStorage().mostRecentInstance().get();
        final GuildPlayer guildPlayer = instance.getPlayer(player);
        if (guildPlayer == null || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.FAIL;

        final long day = player.getWorld().getTimeOfDay() / 24000L;
        if (guildPlayer.getFarmerLastBread() >= day) {
            Translations.FARMER_GOT_BREAD.send(player);
            return ActionResult.SUCCESS;
        }

        // Set the current day and add bread to inventory
        guildPlayer.setFarmerLastBread(day);
        Translations.FARMER_TAKE_BREAD.send(player);

        ItemStack bread = new ItemStack(Items.BREAD, ThreadLocalRandom.current().nextInt(1, 4));
        serverPlayer.getInventory().insertStack(bread);
        if (!bread.isEmpty()) serverPlayer.dropItem(bread, false);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
