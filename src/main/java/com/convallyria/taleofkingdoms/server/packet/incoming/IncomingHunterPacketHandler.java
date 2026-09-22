package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.generic.HunterEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildMasterEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.HireHunterPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class IncomingHunterPacketHandler extends InServerPacketHandler<HireHunterPacket> {

    public IncomingHunterPacketHandler() {
        super(Packets.HIRE_HUNTER, HireHunterPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, HireHunterPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        boolean retire = packet.retire();
        context.taskQueue().execute(() -> TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            final GuildPlayer guildPlayer = instance.getPlayer(player);
            if (guildPlayer == null) {
                reject(player, "Player has no conquest data.");
                return;
            }
            if (!instance.isInGuild(player)) {
                reject(player, "Not in guild.");
                return;
            }

            // Search for banker
            Optional<GuildMasterEntity> entity = instance.getGuildEntity(player.getWorld(), EntityTypes.GUILDMASTER);
            if (entity.isEmpty()) {
                reject(player, "Guildmaster entity not present in guild.");
                return;
            }

            if (retire) {
                if (guildPlayer.getHunters().isEmpty()) {
                    reject(player, "No hunters to retire.");
                    return;
                }

                HunterEntity hunterEntity = null;
                for (UUID hunterId : List.copyOf(guildPlayer.getHunters())) {
                    Entity storedEntity = player.getServerWorld().getEntity(hunterId);
                    if (storedEntity instanceof HunterEntity hunter && hunter.isAlive() && !hunter.isRemoved()) {
                        hunterEntity = hunter;
                        break;
                    }
                    guildPlayer.getHunters().remove(hunterId);
                    TaleOfKingdoms.LOGGER.info("Removed stale hunter reference {} for {}", hunterId, player.getName().getString());
                }
                if (hunterEntity == null) {
                    reject(player, "Hunter entity returned null.");
                    ServerConquestInstance.sync(player, instance);
                    return;
                }

                if (!guildPlayer.tryCreditCoins(750)) {
                    reject(player, "Unable to refund hunter.");
                    return;
                }
                hunterEntity.remove(Entity.RemovalReason.DISCARDED);
                guildPlayer.getHunters().remove(hunterEntity.getUuid());
                ServerConquestInstance.sync(player, instance);
                return;
            }

            if (guildPlayer.getCoins() < 1500) {
                reject(player, "Not enough coins.");
                return;
            }

            HunterEntity hunterEntity = EntityUtils.spawnEntity(EntityTypes.HUNTER, player, entity.get().getBlockPos());
            if (hunterEntity == null) {
                reject(player, "Hunter entity could not be spawned.");
                return;
            }
            if (!guildPlayer.trySpendCoins(1500)) {
                hunterEntity.remove(Entity.RemovalReason.DISCARDED);
                reject(player, "Unable to charge hunter cost.");
                return;
            }
            hunterEntity.setOwner(player);
            guildPlayer.getHunters().add(hunterEntity.getUuid());
            ServerConquestInstance.sync(player, instance);
        }));
    }
}
