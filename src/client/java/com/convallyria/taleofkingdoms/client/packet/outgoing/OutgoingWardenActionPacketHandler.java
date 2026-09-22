package com.convallyria.taleofkingdoms.client.packet.outgoing;

import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.WardenActionPacket;

public final class OutgoingWardenActionPacketHandler extends OutClientPacketHandler<WardenActionPacket> {

    public OutgoingWardenActionPacketHandler() {
        super(Packets.WARDEN_ACTION, WardenActionPacket.CODEC);
    }
}
