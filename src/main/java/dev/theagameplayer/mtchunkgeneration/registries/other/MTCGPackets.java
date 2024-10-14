package dev.theagameplayer.mtchunkgeneration.registries.other;

import dev.theagameplayer.mtchunkgeneration.MTChunkGenerationMod;
import dev.theagameplayer.mtchunkgeneration.network.MTGPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MTCGPackets {
	public static final void registerPackets(final RegisterPayloadHandlersEvent pEvent) {
		final PayloadRegistrar registrar = pEvent.registrar(MTChunkGenerationMod.MODID).versioned("1.0.0").optional();
		registrar.playToClient(MTGPacket.TYPE, MTGPacket.STREAM_CODEC, MTGPacket::handle);
	}
	
	public static final void sendToClient(final CustomPacketPayload pPacket, final ServerPlayer pPlayer) {
		PacketDistributor.sendToPlayer(pPlayer, pPacket);
	}
	
	public static final void sendToClientsIn(final CustomPacketPayload pPacket, final ServerLevel levelIn) {
		PacketDistributor.sendToPlayersInDimension(levelIn, pPacket);
	}
	
	public static final void sendToAllClients(final CustomPacketPayload pPacket) {
		PacketDistributor.sendToAllPlayers(pPacket);
	}
}
