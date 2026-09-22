package com.convallyria.taleofkingdoms.client.gui.entity.shop.widget;

import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.ShopEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.ToggleSellGuiPacket;
import com.convallyria.taleofkingdoms.common.shop.ShopItem;
import com.convallyria.taleofkingdoms.common.shop.SellScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ShopScreenInterface {

    ShopItem getSelectedItem();

    void setSelectedItem(ShopItem selectedItem);

    default void openSellGui(ShopEntity entity, PlayerEntity player) {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (MinecraftClient.getInstance().getServer() == null) {
            api.getClientPacket(Packets.TOGGLE_SELL_GUI)
                    .sendPacket(player, new ToggleSellGuiPacket(false, entity.getGUIType()));
            return;
        }

        api.executeOnServerEnvironment(server -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
            if (serverPlayer != null) serverPlayer.openHandledScreen(SellScreenHandler.createFactory());
        });
    }
}
