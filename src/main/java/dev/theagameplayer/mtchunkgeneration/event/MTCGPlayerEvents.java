package dev.theagameplayer.mtchunkgeneration.event;

import java.util.Optional;

import dev.theagameplayer.mtchunkgeneration.network.MTGPacket;
import dev.theagameplayer.mtchunkgeneration.registries.other.MTCGPackets;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class MTCGPlayerEvents {
	public static final void loggedIn(final PlayerEvent.PlayerLoggedInEvent pEvent) {
		if (pEvent.getEntity() instanceof ServerPlayer player)
			sendMTGPacket(player);
	}
	
	public static final void playerRespawn(final PlayerEvent.PlayerRespawnEvent pEvent) {
		if (pEvent.getEntity() instanceof ServerPlayer player) {
			if (pEvent.isEndConquered()) {
				sendMTGPacket(player);
				return;
			}
			final Optional<GlobalPos> deathPos = player.getLastDeathLocation();
			if (deathPos.isPresent() && !deathPos.get().dimension().equals(player.level().dimension()))
				sendMTGPacket(player);
		}
	}
	
	public static final void changedDimension(final PlayerEvent.PlayerChangedDimensionEvent pEvent) {
		if (pEvent.getEntity() instanceof ServerPlayer player && pEvent.getTo().equals(player.level().dimension()))
			sendMTGPacket(player);
	}
	
	private static final void sendMTGPacket(final ServerPlayer pPlayer) {
		if (!pPlayer.connection.hasChannel(MTGPacket.TYPE)) return;
		MTCGPackets.sendToClient(new MTGPacket(pPlayer.serverLevel().getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator), pPlayer);
	}
}
