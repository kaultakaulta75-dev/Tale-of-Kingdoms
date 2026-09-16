package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.guild.CityBuilderEntity;
import com.convallyria.taleofkingdoms.common.kingdom.KingdomTier;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.kingdom.builds.BuildCosts;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.UpgradeKingdomPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Optional;

public final class IncomingUpgradeKingdomPacketHandler extends InServerPacketHandler<UpgradeKingdomPacket> {

    public IncomingUpgradeKingdomPacketHandler() {
        super(Packets.UPGRADE_KINGDOM, UpgradeKingdomPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, UpgradeKingdomPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        context.taskQueue().execute(() -> TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            final Entity entity = player.getWorld().getEntityById(packet.entityId());
            if (!(entity instanceof CityBuilderEntity cityBuilder) || player.distanceTo(cityBuilder) > 5) {
                reject(player, "Invalid entity ID / Distance");
                return;
            }

            final GuildPlayer guildPlayer = instance.getPlayer(player.getUuid());
            if (guildPlayer == null || guildPlayer.getKingdom() == null) {
                reject(player, "No kingdom");
                return;
            }

            final PlayerKingdom kingdom = guildPlayer.getKingdom();
            final Optional<KingdomTier> nextTier = kingdom.getTier().next();
            if (nextTier.isEmpty()) {
                reject(player, "Kingdom is already at maximum tier");
                return;
            }
            if (cityBuilder.getWood() < 320 || cityBuilder.getStone() < 320) {
                reject(player, "Not enough resources");
                return;
            }
            if (Arrays.stream(BuildCosts.values()).anyMatch(cost -> cost.getTier() == kingdom.getTier() && !kingdom.hasBuilt(cost))) {
                reject(player, "Required tier buildings are missing");
                return;
            }
            if (!kingdom.beginConstruction()) {
                reject(player, "Another kingdom construction is already in progress");
                return;
            }

            final KingdomTier next = nextTier.get();
            final BlockPos offsetPos = kingdom.getOrigin().subtract(next.getOffset());
            TaleOfKingdoms.getAPI().getSchematicHandler().pasteSchematic(next.getSchematic(), player, offsetPos).whenComplete((box, error) -> {
                kingdom.finishConstruction();
                if (error != null) {
                    TaleOfKingdoms.LOGGER.error("Failed to upgrade kingdom for {}", player.getName().getString(), error);
                    player.sendMessage(Text.translatable("message.taleofkingdoms.kingdom.upgrade_failed"), false);
                    ServerConquestInstance.sync(player, instance);
                    return;
                }

                kingdom.setStart(new BlockPos(box.getMaxX(), box.getMaxY(), box.getMaxZ()));
                kingdom.setEnd(new BlockPos(box.getMinX(), box.getMinY(), box.getMinZ()));
                kingdom.setTier(next);
                cityBuilder.getInventory().removeItem(Items.OAK_LOG, 320);
                cityBuilder.getInventory().removeItem(Items.COBBLESTONE, 320);
                ServerConquestInstance.sync(player, instance);
            });
        }));
    }
}
