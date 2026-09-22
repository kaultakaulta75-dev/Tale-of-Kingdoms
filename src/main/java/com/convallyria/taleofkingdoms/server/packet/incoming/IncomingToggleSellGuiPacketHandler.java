package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.ShopEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.ToggleSellGuiPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.shop.SellScreenHandler;
import com.convallyria.taleofkingdoms.common.shop.ShopParser;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public final class IncomingToggleSellGuiPacketHandler extends InServerPacketHandler<ToggleSellGuiPacket> {

    public IncomingToggleSellGuiPacketHandler() {
        super(Packets.TOGGLE_SELL_GUI, ToggleSellGuiPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, ToggleSellGuiPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        boolean close = packet.close();
        ShopParser.GUI type = packet.type();
        context.taskQueue().execute(() -> {
            // Older clients used a close packet to delete a temporary world block.
            // The menu is now virtual, so closing it needs no server-side action.
            if (close) return;

            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                if (!instance.isInGuild(player)) {
                    reject(player, "Not in guild.");
                    return;
                }

                // Search for either foodshop, itemshop, or blacksmith in the guild
                Optional<? extends ShopEntity> entity = Optional.empty();
                switch (type) {
                    case BLACKSMITH -> entity = instance.search(player, player.getWorld(), EntityTypes.BLACKSMITH);
                    case FOOD -> entity = instance.search(player, player.getWorld(), EntityTypes.FOODSHOP);
                    case ITEM -> entity = instance.search(player, player.getWorld(), EntityTypes.ITEM_SHOP);
                }

                if (entity.isEmpty()) {
                    reject(player, "Shop entity not present in guild.");
                    return;
                }

                player.openHandledScreen(SellScreenHandler.createFactory());
            });
        });
    }
}
