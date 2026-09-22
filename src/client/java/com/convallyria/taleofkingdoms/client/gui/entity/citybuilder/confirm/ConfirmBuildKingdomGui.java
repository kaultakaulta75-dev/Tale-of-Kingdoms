package com.convallyria.taleofkingdoms.client.gui.entity.citybuilder.confirm;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.TaleOfKingdomsClient;
import com.convallyria.taleofkingdoms.client.gui.entity.citybuilder.BaseCityBuilderScreen;
import com.convallyria.taleofkingdoms.common.entity.guild.CityBuilderEntity;
import com.convallyria.taleofkingdoms.common.generator.util.StructurePlacementUtils;
import com.convallyria.taleofkingdoms.common.kingdom.KingdomTier;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.BuildKingdomPacket;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.common.world.guild.GuildQuestProgression;
import com.convallyria.taleofkingdoms.managers.SoundManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Positioning;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class ConfirmBuildKingdomGui extends BaseCityBuilderScreen {

    private final PlayerEntity player;
    private final CityBuilderEntity entity;
    private final ConquestInstance instance;

    public ConfirmBuildKingdomGui(PlayerEntity player, CityBuilderEntity entity, ConquestInstance instance) {
        super(DataSource.asset(Identifier.of(TaleOfKingdoms.MODID, "citybuilder_confirm_build_kingdom_model")));
        this.player = player;
        this.entity = entity;
        this.instance = instance;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.child(
            Components.button(Text.translatable("menu.taleofkingdoms.citybuilder.build"), c -> {
                // Close current screen, calculate paste position, and add their kingdom
                MinecraftClient.getInstance().currentScreen.close();
                if (MinecraftClient.getInstance().getServer() == null) {
                    TaleOfKingdomsClient.getAPI().getClientPacket(Packets.BUILD_KINGDOM)
                            .sendPacket(player, new BuildKingdomPacket(entity.getId()));
                    return;
                }

                TaleOfKingdoms.getAPI().executeOnServerEnvironment(server -> {
                    final ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
                    if (serverPlayer == null
                            || !(serverPlayer.getWorld().getEntityById(entity.getId()) instanceof CityBuilderEntity cityBuilderServer)
                            || serverPlayer.distanceTo(cityBuilderServer) > 5) return;
                    final GuildPlayer guildPlayer = instance.getPlayer(serverPlayer);
                    if (!GuildQuestProgression.canFoundKingdom(instance, guildPlayer)) {
                        if (guildPlayer != null) serverPlayer.sendMessage(GuildQuestProgression.getCurrentObjective(instance, guildPlayer), false);
                        return;
                    }

                    int distance = (int) instance.getCentre().distanceTo(serverPlayer.getPos());
                    if (distance < 500) {
                        Translations.CITYBUILDER_DISTANCE.send(serverPlayer, distance, 500);
                        return;
                    }

                    BlockPos pos = serverPlayer.getBlockPos().subtract(new Vec3i(0, 25, 85));
                    StructurePlacementUtils.FoundationProblem foundation = TaleOfKingdoms.getAPI()
                            .getSchematicHandler().inspectFoundation(Schematic.TIER_1_KINGDOM, serverPlayer, pos, BlockRotation.NONE);
                    if (!foundation.isSuitable()) {
                        serverPlayer.sendMessage(Text.translatable(foundation.getTranslationKey()), false);
                        return;
                    }
                    BlockPos expandedPos = pos.subtract(KingdomTier.TIER_TWO.getOffset());
                    foundation = TaleOfKingdoms.getAPI().getSchematicHandler()
                            .inspectFoundation(Schematic.TIER_2_KINGDOM, serverPlayer, expandedPos, BlockRotation.NONE);
                    if (!foundation.isSuitable()) {
                        serverPlayer.sendMessage(Text.translatable(foundation.getTranslationKey()), false);
                        return;
                    }

                    final PlayerKingdom playerKingdom = new PlayerKingdom(pos);
                    playerKingdom.beginConstruction();
                    guildPlayer.setKingdom(playerKingdom);

                    TaleOfKingdoms.getAPI().getSchematicHandler().pasteSchematic(Schematic.TIER_1_KINGDOM, serverPlayer, pos, SchematicOptions.ALIGN_TO_TERRAIN).whenComplete((box, error) -> {
                        playerKingdom.finishConstruction();
                        if (error != null) {
                            if (guildPlayer.getKingdom() == playerKingdom) guildPlayer.setKingdom(null);
                            TaleOfKingdoms.LOGGER.error("Failed to build kingdom for {}", serverPlayer.getName().getString(), error);
                            serverPlayer.sendMessage(Text.translatable("message.taleofkingdoms.kingdom.build_failed"), false);
                            return;
                        }
                        playerKingdom.setStart(new BlockPos(box.getMaxX(), box.getMaxY(), box.getMaxZ()));
                        playerKingdom.setEnd(new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()));
                        playerKingdom.setOrigin(new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()));

                        cityBuilderServer.settleInKingdom(serverPlayer, playerKingdom);
                        serverPlayer.sendMessage(Text.translatable("message.taleofkingdoms.kingdom.founded"), false);
                    });
                });
                player.playSoundToPlayer(TaleOfKingdoms.getAPI().getManager(SoundManager.class).getSound(SoundManager.TOKSound.TOKTHEME), SoundCategory.MUSIC, 0.1f, 1f);
            })
            .positioning(Positioning.relative(50, 67))
        );

        rootComponent.child(
            Components.button(
                Text.translatable("menu.taleofkingdoms.generic.cancel"),
                (ButtonComponent button) -> this.close()
            )
            .positioning(Positioning.relative(50, 75))
        );
    }
}
