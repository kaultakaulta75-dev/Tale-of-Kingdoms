package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.FixGuildPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.utils.InventoryUtils;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class IncomingFixGuildPacketHandler extends InServerPacketHandler<FixGuildPacket> {

    private long lastRebuild;

    public IncomingFixGuildPacketHandler() {
        super(Packets.FIX_GUILD, FixGuildPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, FixGuildPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        String playerContext = getPacket().toString() + " @ <" + player.getName().getString() + ":" + player.getIp() + ">";
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        context.taskQueue().execute(() -> api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            if (instance.isUnderAttack()) {
                reject(player, "Guild is under attack");
                return;
            }

            PlayerInventory playerInventory = player.getInventory();
            if (InventoryUtils.count(playerInventory, ItemTags.LOGS) < 64) {
                reject(player, "Inventory requirement not met. Data mismatch?");
                return;
            }

            final GuildPlayer guildPlayer = instance.getPlayer(player);
            if (guildPlayer == null || guildPlayer.getCoins() < 3000) {
                reject(player, "Coin requirement not met. Data mismatch?");
                return;
            }

            TaleOfKingdoms.LOGGER.info("Guild rebuild requested " + playerContext + ".");
            long now = System.currentTimeMillis();
            long seconds = (now - this.lastRebuild) / 1000;
            if (seconds <= 60) {
                reject(player, "Rebuilt too recently.");
                long secondsLeft = 60 - seconds;
                player.sendMessage(Text.literal("The guild was only rebuilt " + seconds + " seconds ago! Please wait " + secondsLeft + " more seconds."), false);
                return;
            }
            if (!instance.beginGuildRebuild()) {
                reject(player, "Guild rebuild already in progress.");
                return;
            }
            if (!guildPlayer.trySpendCoins(3000)) {
                instance.finishGuildRebuild();
                reject(player, "Unable to charge rebuild cost.");
                return;
            }
            if (!InventoryUtils.remove(playerInventory, ItemTags.LOGS, 64)) {
                guildPlayer.tryCreditCoins(3000);
                instance.finishGuildRebuild();
                reject(player, "Unable to remove rebuild materials.");
                return;
            }

            ServerConquestInstance.sync(player, instance);
            instance.rebuild(player, api, SchematicOptions.IGNORE_DEFENDERS).whenComplete((box, error) -> {
                instance.finishGuildRebuild();
                if (error != null) {
                    guildPlayer.tryCreditCoins(3000);
                    ItemStack refund = new ItemStack(Items.OAK_LOG, 64);
                    playerInventory.insertStack(refund);
                    if (!refund.isEmpty()) player.dropItem(refund, false);
                    player.sendMessage(Text.translatable("message.taleofkingdoms.guild.rebuild_failed"), false);
                    TaleOfKingdoms.LOGGER.error("Guild rebuild failed for {}", player.getName().getString(), error);
                } else {
                    this.lastRebuild = System.currentTimeMillis();
                    player.sendMessage(Text.translatable("message.taleofkingdoms.guild.repair_success"), false);
                }
                ServerConquestInstance.sync(player, instance);
            });
        }));
    }
}
