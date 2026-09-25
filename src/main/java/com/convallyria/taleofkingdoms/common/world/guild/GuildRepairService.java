package com.convallyria.taleofkingdoms.common.world.guild;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.utils.InventoryUtils;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Authoritative guild repair rules shared by integrated and dedicated servers.
 * Keeping payment, cooldown and attack checks here prevents single-player UI
 * actions from silently following different rules from network requests.
 */
public final class GuildRepairService {

    private static final int COIN_COST = 3000;
    private static final int LOG_COST = 64;

    private GuildRepairService() {
    }

    public static void requestRepair(ServerPlayerEntity player,
                                     ConquestInstance instance,
                                     TaleOfKingdomsAPI api,
                                     Consumer<String> reject,
                                     Runnable sync) {
        if (instance.isUnderAttack()) {
            reject.accept("Guild is under attack");
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (InventoryUtils.count(inventory, ItemTags.LOGS) < LOG_COST) {
            reject.accept("Inventory requirement not met");
            return;
        }

        GuildPlayer guildPlayer = instance.getPlayer(player);
        if (guildPlayer == null || guildPlayer.getCoins() < COIN_COST) {
            reject.accept("Coin requirement not met");
            return;
        }

        long secondsLeft = instance.getGuildRepairCooldownSeconds(System.currentTimeMillis());
        if (secondsLeft > 0) {
            reject.accept("Guild repaired too recently");
            player.sendMessage(Text.translatable("message.taleofkingdoms.guild.repair_cooldown", secondsLeft), false);
            return;
        }
        if (!instance.beginGuildRebuild()) {
            reject.accept("Guild rebuild already in progress");
            return;
        }
        if (!guildPlayer.trySpendCoins(COIN_COST)) {
            instance.finishGuildRebuild();
            reject.accept("Unable to charge rebuild cost");
            return;
        }
        if (!InventoryUtils.remove(inventory, ItemTags.LOGS, LOG_COST)) {
            guildPlayer.tryCreditCoins(COIN_COST);
            instance.finishGuildRebuild();
            reject.accept("Unable to remove rebuild materials");
            return;
        }

        sync.run();
        instance.rebuild(player, api, SchematicOptions.IGNORE_DEFENDERS).whenComplete((box, error) -> {
            if (error != null) {
                guildPlayer.tryCreditCoins(COIN_COST);
                ItemStack refund = new ItemStack(Items.OAK_LOG, LOG_COST);
                inventory.insertStack(refund);
                if (!refund.isEmpty()) player.dropItem(refund, false);
                player.sendMessage(Text.translatable("message.taleofkingdoms.guild.rebuild_failed"), false);
                TaleOfKingdoms.LOGGER.error("Guild rebuild failed for {}", player.getName().getString(), error);
            } else {
                instance.recordSuccessfulGuildRepair(System.currentTimeMillis());
                player.sendMessage(Text.translatable("message.taleofkingdoms.guild.repair_success"), false);
            }
            instance.finishGuildRebuild();
            sync.run();
        });
    }
}
