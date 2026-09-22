package com.convallyria.taleofkingdoms.client.gui.entity;

import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.client.gui.ScreenTOK;
import com.convallyria.taleofkingdoms.common.entity.guild.InnkeeperEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.InnkeeperActionPacket;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.utils.BlockUtils;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class InnkeeperScreen extends ScreenTOK {

    private final PlayerEntity player;
    private final InnkeeperEntity entity;
    private final ConquestInstance instance;

    public InnkeeperScreen(PlayerEntity player, InnkeeperEntity entity, ConquestInstance instance) {
        super("taleofkingdoms.menu.innkeeper.name");
        this.player = player;
        this.entity = entity;
        this.instance = instance;
        Translations.INNKEEPER_REST.send(player);
    }

    @Override
    public void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.taleofkingdoms.innkeeper.rest"), widget -> {
            this.close();
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            ConquestInstance conquestInstance = api.getConquestInstanceStorage().mostRecentInstance().orElse(null);
            if (conquestInstance == null) return;

            final GuildPlayer guildPlayer = conquestInstance.getPlayer(player.getUuid());
            if (guildPlayer == null || guildPlayer.getCoins() < 10) {
                Translations.INNKEEPER_NOT_ENOUGH_COINS.send(player);
                return;
            }

            if (MinecraftClient.getInstance().getServer() == null) {
                api.getClientPacket(Packets.INNKEEPER_HIRE_ROOM)
                        .sendPacket(player, new InnkeeperActionPacket(true));
                return;
            }

            api.executeOnServerEnvironment(server -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
                if (serverPlayer == null) return;
                GuildPlayer serverGuildPlayer = conquestInstance.getPlayer(serverPlayer);
                if (serverGuildPlayer == null) return;
                BlockPos rest = BlockUtils.locateRestingPlace(conquestInstance, serverPlayer);
                if (rest == null) {
                    serverPlayer.sendMessage(Text.translatable("menu.taleofkingdoms.innkeeper.no_rooms"));
                    return;
                }
                if (!serverGuildPlayer.trySpendCoins(10)) {
                    Translations.INNKEEPER_NOT_ENOUGH_COINS.send(serverPlayer);
                    return;
                }

                Translations.INNKEEPER_REST_SUCCESS.send(serverPlayer);
                advanceTime(serverPlayer.getServerWorld(), 1000);
                serverPlayer.requestTeleport(rest.getX() + 0.5, rest.getY(), rest.getZ() + 0.5);
                serverPlayer.refreshPositionAfterTeleport(rest.getX() + 0.5, rest.getY(), rest.getZ() + 0.5);
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 1));
                serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            });
        }).dimensions(this.width / 2 - 75, this.height / 4 + 50, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.taleofkingdoms.innkeeper.wait"), widget -> {
            this.close();
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            ConquestInstance conquestInstance = api.getConquestInstanceStorage().mostRecentInstance().orElse(null);
            if (conquestInstance == null) return;

            final GuildPlayer guildPlayer = conquestInstance.getPlayer(player.getUuid());
            if (guildPlayer == null || guildPlayer.getCoins() < 10) {
                Translations.INNKEEPER_NOT_ENOUGH_COINS.send(player);
                return;
            }

            if (MinecraftClient.getInstance().getServer() == null) {
                api.getClientPacket(Packets.INNKEEPER_HIRE_ROOM)
                        .sendPacket(player, new InnkeeperActionPacket(false));
                return;
            }

            api.executeOnServerEnvironment(server -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
                if (serverPlayer == null) return;
                GuildPlayer serverGuildPlayer = conquestInstance.getPlayer(serverPlayer);
                if (serverGuildPlayer == null || !serverGuildPlayer.trySpendCoins(10)) {
                    Translations.INNKEEPER_NOT_ENOUGH_COINS.send(serverPlayer);
                    return;
                }
                Translations.INNKEEPER_WAIT_SUCCESS.send(serverPlayer);
                advanceTime(serverPlayer.getServerWorld(), 13000);
            });
        }).dimensions(this.width / 2 - 75, this.height / 4 + 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.taleofkingdoms.generic.exit"), widget -> {
            this.close();
            Translations.INNKEEPER_LEAVE.send(player);
        }).dimensions(this.width / 2 - 75, this.height / 4 + 100, 150, 20).build());
    }

    private void advanceTime(ServerWorld world, long targetTime) {
        long currentTime = world.getTimeOfDay();
        long currentDayTime = Math.floorMod(currentTime, 24000L);
        long advance = targetTime - currentDayTime;
        if (advance <= 0) advance += 24000L;
        world.setTimeOfDay(currentTime + advance);
    }

    @Override
    public void render(DrawContext context, int par1, int par2, float par3) {
        super.render(context, par1, par2, par3);
        context.drawCenteredTextWithShadow(this.textRenderer, "Time flies when you rest...", this.width / 2, this.height / 4 - 25, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Waiting or resting costs 10 coins.", this.width / 2, this.height / 2 + 100, 0XFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
