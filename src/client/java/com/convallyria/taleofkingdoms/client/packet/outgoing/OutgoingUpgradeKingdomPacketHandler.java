package com.convallyria.taleofkingdoms.client.packet.outgoing;

import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.c2s.UpgradeKingdomPacket;

public final class OutgoingUpgradeKingdomPacketHandler extends OutClientPacketHandler<UpgradeKingdomPacket> {

    public OutgoingUpgradeKingdomPacketHandler() {
        super(Packets.UPGRADE_KINGDOM, UpgradeKingdomPacket.CODEC);
    }
}
