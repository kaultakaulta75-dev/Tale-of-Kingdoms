package com.convallyria.taleofkingdoms.client.gui.entity.kingdom;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.TaleOfKingdomsClient;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WardenEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.action.WardenAction;
import com.convallyria.taleofkingdoms.common.packet.c2s.WardenActionPacket;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WardenScreen extends BaseUIModelScreen<FlowLayout> {

    private final PlayerEntity player;
    private final WardenEntity entity;
    private final ConquestInstance instance;
    private LabelComponent totalMoney;

    public WardenScreen(PlayerEntity player, WardenEntity entity, ConquestInstance instance) {
        super(FlowLayout.class, BaseUIModelScreen.DataSource.asset(Identifier.of(TaleOfKingdoms.MODID, "warden_ui_model")));
        this.player = player;
        this.entity = entity;
        this.instance = instance;
        player.sendMessage(Text.translatable("entity_type.taleofkingdoms.warden.gui.open"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout inner = rootComponent.childById(FlowLayout.class, "inner");

        this.totalMoney = inner.childById(LabelComponent.class, "total-money");
        this.totalMoney.text(Text.translatable("menu.taleofkingdoms.warden.total_money", instance.getPlayer(player).getCoins()));

        inner.childById(ButtonComponent.class, "recruit-knight-button").onPress(b -> {
            handleAction(WardenAction.RECRUIT_WARRIOR);
        });

        inner.childById(ButtonComponent.class, "recruit-archer-button").onPress(b -> {
            handleAction(WardenAction.RECRUIT_ARCHER);
        });

        inner.childById(ButtonComponent.class, "recall-defenders-button").onPress(b -> {
            handleAction(WardenAction.RECALL_SOLDIERS);
        });

        inner.childById(ButtonComponent.class, "exit-button").onPress(b -> this.close());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (totalMoney != null) {
            totalMoney.text(Text.translatable("menu.taleofkingdoms.warden.total_money", instance.getPlayer(player).getCoins()));
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void handleAction(WardenAction action) {
        if (MinecraftClient.getInstance().getServer() == null) {
            TaleOfKingdomsClient.getAPI().getClientPacket(Packets.WARDEN_ACTION)
                    .sendPacket(player, new WardenActionPacket(entity.getId(), action));
            return;
        }

        TaleOfKingdoms.getAPI().executeOnServerEnvironment(server -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
            if (serverPlayer == null || !(serverPlayer.getWorld().getEntityById(entity.getId()) instanceof WardenEntity serverWarden)) return;
            switch (action) {
                case RECRUIT_WARRIOR -> serverWarden.buySoldier(serverPlayer, instance, (byte) 1);
                case RECRUIT_ARCHER -> serverWarden.buySoldier(serverPlayer, instance, (byte) 2);
                case RECALL_SOLDIERS -> serverWarden.recallSoldiers(serverPlayer, instance);
            }
        });
    }

    @Override
    public void close() {
        super.close();
        player.sendMessage(Text.translatable("entity_type.taleofkingdoms.warden.gui.close"));
    }
}
