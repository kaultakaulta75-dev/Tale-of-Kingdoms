package com.convallyria.taleofkingdoms.server.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.guild.InnkeeperEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.InnkeeperActionPacket;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.utils.BlockUtils;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public final class IncomingInnkeeperPacketHandler extends InServerPacketHandler<InnkeeperActionPacket> {

    public IncomingInnkeeperPacketHandler() {
        super(Packets.INNKEEPER_HIRE_ROOM, InnkeeperActionPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, InnkeeperActionPacket packet) {
        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
        boolean resting = packet.resting();
        context.taskQueue().execute(() -> TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            if (!instance.isInGuild(player)) {
                reject(player, "Not in guild.");
                return;
            }

            // Search for innkeeper
            Optional<InnkeeperEntity> entity = instance.getGuildEntity(player.getWorld(), EntityTypes.INNKEEPER);
            if (entity.isEmpty()) {
                reject(player, "Innkeeper entity not present in guild.");
                return;
            }

            final GuildPlayer guildPlayer = instance.getPlayer(player);
            if (guildPlayer == null) {
                reject(player, "Player has no conquest data.");
                return;
            }

            int coins = 10;
            if (resting) {
                BlockPos rest = BlockUtils.locateRestingPlace(instance, player);
                if (rest == null) {
                    reject(player, "No rooms available.");
                    return;
                }
                if (!guildPlayer.trySpendCoins(coins)) {
                    reject(player, "Not enough coins.");
                    return;
                }

                advanceTime(player.getServerWorld(), 1000);
                player.requestTeleport(rest.getX() + 0.5, rest.getY(), rest.getZ() + 0.5);
                player.refreshPositionAfterTeleport(rest.getX() + 0.5, rest.getY(), rest.getZ() + 0.5);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                ServerConquestInstance.sync(player, instance);
                return;
            }

            if (!guildPlayer.trySpendCoins(coins)) {
                reject(player, "Not enough coins.");
                return;
            }
            advanceTime(player.getServerWorld(), 13000);
            ServerConquestInstance.sync(player, instance);
        }));
    }

    private void advanceTime(ServerWorld world, long targetTime) {
        long currentTime = world.getTimeOfDay();
        long currentDayTime = Math.floorMod(currentTime, 24000L);
        long advance = targetTime - currentDayTime;
        if (advance <= 0) advance += 24000L;
        world.setTimeOfDay(currentTime + advance);
    }
}
