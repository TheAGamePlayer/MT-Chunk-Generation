package dev.theagameplayer.mtchunkgeneration.client.gui.components.toasts;

import dev.theagameplayer.mtchunkgeneration.MTChunkGenerationMod;
import dev.theagameplayer.mtchunkgeneration.config.MTCGConfigValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;

public final class MTGToast implements Toast {
	private static final ResourceLocation MTG_ON = MTChunkGenerationMod.namespace("toast/mtgon");
	private static final ResourceLocation MTG_OFF = MTChunkGenerationMod.namespace("toast/mtgoff");
	private final ResourceLocation background;

	private MTGToast(final boolean pIsOn) {
		this.background = pIsOn ? MTG_ON : MTG_OFF;
	}
	
	public static final Runnable sendToast(final boolean pIsOn) {
		return () -> {
			if (MTCGConfigValues.client.useMTGBanners)
				Minecraft.getInstance().getToasts().addToast(new MTGToast(pIsOn));
		};
	}
	
	@Override
	public final int width() {
		return 100;
	}
	
	@Override
	public final int height() {
		return 24;
	}
	
	@Override
	public final Visibility render(final GuiGraphics pGuiGraphics, final ToastComponent pToastComponent, final long pTimeSinceLastVisible) {
		pGuiGraphics.blitSprite(this.background, 0, 0, this.width(), this.height());
		return (double)pTimeSinceLastVisible >= 5000.0D * pToastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

}
