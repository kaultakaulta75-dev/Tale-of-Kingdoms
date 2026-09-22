package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WardenEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.WardenActionPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class IncomingWardenActionPacketHandler extends InServerPacketHandler<WardenActionPacket> {

    public IncomingWardenActionPacketHandler() {
        super(Packets.WARDEN_ACTION, WardenActionPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, WardenActionPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        context.taskQueue().execute(() -> TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            Entity entity = player.getWorld().getEntityById(packet.entityId());
            if (!(entity instanceof WardenEntity warden) || player.distanceTo(warden) > 5) {
                reject(player, "Invalid entity ID / Distance");
                return;
            }
            GuildPlayer guildPlayer = instance.getPlayer(player);
            if (guildPlayer == null || guildPlayer.getKingdom() == null || packet.action() == null) {
                reject(player, "Invalid kingdom or action");
                return;
            }

            boolean success = switch (packet.action()) {
                case RECRUIT_WARRIOR -> warden.buySoldier(player, instance, (byte) 1);
                case RECRUIT_ARCHER -> warden.buySoldier(player, instance, (byte) 2);
                case RECALL_SOLDIERS -> {
                    warden.recallSoldiers(player, instance);
                    yield true;
                }
            };
            if (!success) {
                reject(player, "Warden action failed");
                return;
            }
            ServerConquestInstance.sync(player, instance);
        }));
    }
}
