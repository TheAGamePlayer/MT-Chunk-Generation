package dev.theagameplayer.mtchunkgeneration.network;

import dev.theagameplayer.mtchunkgeneration.MTChunkGenerationMod;
import dev.theagameplayer.mtchunkgeneration.client.gui.components.toasts.MTGToast;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MTGPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MTGPacket> TYPE = new CustomPacketPayload.Type<>(MTChunkGenerationMod.namespace("mtg"));
	public static final StreamCodec<FriendlyByteBuf, MTGPacket> STREAM_CODEC = CustomPacketPayload.codec(MTGPacket::write, MTGPacket::read);
	private final boolean isOn;

	public MTGPacket(final boolean pIsOn) {
		this.isOn = pIsOn;
	}
	
	public final void write(final FriendlyByteBuf pBuf) {
		pBuf.writeBoolean(this.isOn);
	}
	
	public static final MTGPacket read(final FriendlyByteBuf pBuf) {
		return new MTGPacket(pBuf.readBoolean());
	}
	
	public static final void handle(final MTGPacket pPacket, final IPayloadContext pCtx) {
		if (pCtx.flow().isServerbound()) return;
		pCtx.enqueueWork(MTGToast.sendToast(pPacket.isOn));
	}
	
	@Override
	public final Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
