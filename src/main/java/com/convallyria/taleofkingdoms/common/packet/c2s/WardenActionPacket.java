package com.convallyria.taleofkingdoms.common.packet.c2s;

import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.action.WardenAction;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record WardenActionPacket(int entityId, WardenAction action) implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, WardenActionPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, WardenActionPacket::entityId,
            PacketCodecs.indexed(i -> WardenAction.values()[i], WardenAction::ordinal), WardenActionPacket::action,
            WardenActionPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return Packets.WARDEN_ACTION;
    }
}
