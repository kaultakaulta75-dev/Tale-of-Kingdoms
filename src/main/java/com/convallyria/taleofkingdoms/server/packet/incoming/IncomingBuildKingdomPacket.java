package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.guild.CityBuilderEntity;
import com.convallyria.taleofkingdoms.common.generator.util.StructurePlacementUtils;
import com.convallyria.taleofkingdoms.common.kingdom.KingdomTier;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.BuildKingdomPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.common.world.guild.GuildQuestProgression;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class IncomingBuildKingdomPacket extends InServerPacketHandler<BuildKingdomPacket> {

    public IncomingBuildKingdomPacket() {
        super(Packets.BUILD_KINGDOM, BuildKingdomPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, BuildKingdomPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        final int entityId = packet.entityId();
        context.taskQueue().execute(() -> {
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                final Entity entity = player.getWorld().getEntityById(entityId);
                if (!(entity instanceof CityBuilderEntity cityBuilderEntity) || player.distanceTo(cityBuilderEntity) > 5) {
                    reject(player, "Invalid entity ID / Distance");
                    return;
                }

                final GuildPlayer guildPlayer = instance.getPlayer(player);
                if (guildPlayer == null) {
                    reject(player, "No guild player data");
                    return;
                }
                if (guildPlayer.getKingdom() != null) {
                    reject(player, "Kingdom already built");
                    return;
                }

                if (!GuildQuestProgression.canFoundKingdom(instance, guildPlayer)) {
                    player.sendMessage(GuildQuestProgression.getCurrentObjective(instance, guildPlayer), false);
                    reject(player, "Kingdom quest requirements are incomplete");
                    return;
                }

                final Vec3d centre = instance.getCentre();
                boolean isWithin = player.getBlockPos().isWithinDistance(new Vec3i((int) centre.getX(), (int) centre.getY(), (int) centre.getZ()), 500);
                if (isWithin) {
                    reject(player, "Too close to guild");
                    return;
                }

                BlockPos pos = player.getBlockPos().subtract(new Vec3i(0, 25, 85));
                StructurePlacementUtils.FoundationProblem foundation = TaleOfKingdoms.getAPI()
                        .getSchematicHandler().inspectFoundation(Schematic.TIER_1_KINGDOM, player, pos, BlockRotation.NONE);
                if (!foundation.isSuitable()) {
                    player.sendMessage(Text.translatable(foundation.getTranslationKey()), false);
                    reject(player, "Unsuitable castle foundation: " + foundation);
                    return;
                }
                BlockPos expandedPos = pos.subtract(KingdomTier.TIER_TWO.getOffset());
                foundation = TaleOfKingdoms.getAPI().getSchematicHandler()
                        .inspectFoundation(Schematic.TIER_2_KINGDOM, player, expandedPos, BlockRotation.NONE);
                if (!foundation.isSuitable()) {
                    player.sendMessage(Text.translatable(foundation.getTranslationKey()), false);
                    reject(player, "Unsuitable expanded castle foundation: " + foundation);
                    return;
                }

                final PlayerKingdom playerKingdom = new PlayerKingdom(pos);
                playerKingdom.beginConstruction();
                guildPlayer.setKingdom(playerKingdom);

                // Paste their kingdom
                TaleOfKingdoms.getAPI().getSchematicHandler().pasteSchematic(Schematic.TIER_1_KINGDOM, player, pos, SchematicOptions.ALIGN_TO_TERRAIN).whenComplete((box, error) -> {
                    playerKingdom.finishConstruction();
                    if (error != null) {
                        if (guildPlayer.getKingdom() == playerKingdom) guildPlayer.setKingdom(null);
                        TaleOfKingdoms.LOGGER.error("Failed to build kingdom for {}", player.getName().getString(), error);
                        player.sendMessage(Text.translatable("message.taleofkingdoms.kingdom.build_failed"), false);
                        ServerConquestInstance.sync(player, instance);
                        return;
                    }
                    BlockPos start = new BlockPos(box.getMaxX(), box.getMaxY(), box.getMaxZ());
                    BlockPos end = new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ());
                    playerKingdom.setStart(start);
                    playerKingdom.setEnd(end);
                    playerKingdom.setOrigin(new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()));

                    cityBuilderEntity.settleInKingdom(player, playerKingdom);

                    player.sendMessage(Text.translatable("message.taleofkingdoms.kingdom.founded"), false);
                    ServerConquestInstance.sync(player, instance);
                });
            });
        });
    }
}
