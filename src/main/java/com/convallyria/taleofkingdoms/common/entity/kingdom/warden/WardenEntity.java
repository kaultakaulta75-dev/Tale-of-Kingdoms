package com.convallyria.taleofkingdoms.common.entity.kingdom.warden;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.s2c.OpenScreenPacket;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class WardenEntity extends TOKEntity {

    public WardenEntity(@NotNull EntityType<? extends PathAwareEntity> entityType, @NotNull World world) {
        super(entityType, world);
        this.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
    }

    public boolean buySoldier(ServerPlayerEntity player, ConquestInstance instance, byte index) {
        if (index != 1 && index != 2) return false;
        final GuildPlayer guildPlayer = instance.getPlayer(player);
        if (guildPlayer == null || guildPlayer.getCoins() < 1000) return false;

        final PlayerKingdom kingdom = guildPlayer.getKingdom();
        if (kingdom == null) return false;

        EntityType<? extends WardenHireable> type = index == 1 ? EntityTypes.WARRIOR : EntityTypes.ARCHER;
        final WardenHireable soldier = EntityUtils.spawnEntity(type, player, this.getBlockPos());
        if (soldier == null) return false;
        if (!guildPlayer.trySpendCoins(1000)) {
            soldier.remove(Entity.RemovalReason.DISCARDED);
            return false;
        }
        soldier.setOwner(player);
        soldier.toggleFollowGoal(player);
        return true;
    }

    public int recallSoldiers(ServerPlayerEntity player, ConquestInstance instance) {
        final GuildPlayer guildPlayer = instance.getPlayer(player);
        if (guildPlayer == null) return 0;
        final PlayerKingdom kingdom = guildPlayer.getKingdom();
        if (kingdom == null) return 0;

        int recalled = 0;
        for (Entity entity : player.getServerWorld().iterateEntities()) {
            if (!(entity instanceof WardenHireable soldier) || !soldier.isOwnedBy(player)) continue;
            entity.requestTeleport(this.getX(), this.getY(), this.getZ());
            if (!soldier.isFollowingPlayer()) soldier.toggleFollowGoal(player);
            recalled++;
        }
        return recalled;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        if (player.getWorld().isClient()) return ActionResult.SUCCESS;
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null) return ActionResult.FAIL;
        if (api.getConquestInstanceStorage().mostRecentInstance().isEmpty()) return ActionResult.FAIL;
        api.getServerPacket(Packets.OPEN_CLIENT_SCREEN).sendPacket(player, new OpenScreenPacket(OpenScreenPacket.ScreenTypes.WARDEN, this.getId()));
        return ActionResult.SUCCESS;
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
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean damage(DamageSource damageSource, float f) {
        return false;
    }
}
