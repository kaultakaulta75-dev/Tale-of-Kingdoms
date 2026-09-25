package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.FixGuildPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.world.guild.GuildRepairService;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.server.network.ServerPlayerEntity;

public final class IncomingFixGuildPacketHandler extends InServerPacketHandler<FixGuildPacket> {

    public IncomingFixGuildPacketHandler() {
        super(Packets.FIX_GUILD, FixGuildPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, FixGuildPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        String playerContext = getPacket().toString() + " @ <" + player.getName().getString() + ":" + player.getIp() + ">";
        context.taskQueue().execute(() -> TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            TaleOfKingdoms.LOGGER.info("Guild rebuild requested {}.", playerContext);
            GuildRepairService.requestRepair(
                    player,
                    instance,
                    TaleOfKingdoms.getAPI(),
                    reason -> reject(player, reason),
                    () -> ServerConquestInstance.sync(player, instance)
            );
        }));
    }
}
