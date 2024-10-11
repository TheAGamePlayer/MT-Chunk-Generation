package dev.theagameplayer.mtchunkgeneration.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.theagameplayer.mtchunkgeneration.config.MTCGConfigValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

@Mixin(ChunkBorderRenderer.class)
public final class ChunkBorderRendererMixin {
	private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
	private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/renderer/debug/ChunkBorderRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;DDD)V", cancellable = true)
	private final void render(final PoseStack pPoseStack, final MultiBufferSource pBuffer, final double pCamX, final double pCamY, final double pCamZ, final CallbackInfo pCallbackInfo) {
		if (!MTCGConfigValues.client.use3dChunkDebugging) return;
		final Minecraft minecraft = Minecraft.getInstance();
		final Entity entity = minecraft.gameRenderer.getMainCamera().getEntity();
		final ChunkPos chunkPos = entity.chunkPosition();
		final SectionPos sectionPos = SectionPos.of(entity.blockPosition());
		final float xMin = (float)((double)chunkPos.getMinBlockX() - pCamX);
		final float yMin = (float)((double)sectionPos.minBlockY() - pCamY);
		final float zMin = (float)((double)chunkPos.getMinBlockZ() - pCamZ);
		VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.debugLineStrip(1.0));
		final Matrix4f matrix4f = pPoseStack.last().pose();
		//Red Lines
		for (int x = -16; x <= 32; x += 16) {
			final int xOffset = x == 32 ? 0 : 16;
			for (int y = -16; y <= 32; y += 16) {
				final int yOffset = y == 32 ? 0 : 16;
				for (int z = -16; z <= 32; z += 16) {
					final int zOffset = z == 32 ? 0 : 16;
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + xOffset + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + xOffset + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + yOffset + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + yOffset + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + zOffset + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.5F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, zMin + zOffset + (float)z).setColor(1.0F, 0.0F, 0.0F, 0.0F);
				}
			}
		}
		//Yellow & Cyan Lines
		for (int x = 2; x < 16; x += 2) {
			final int color = x % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)x, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)x, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin + (float)x, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin + (float)x, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)x, zMin + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)x, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin + (float)x, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin + (float)x, zMin + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int x = 2; x < 16; x += 2) {
			final int color = x % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zMin + (float)x).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zMin + (float)x).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin, zMin + (float)x).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16, yMin, zMin + (float)x).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16.0F, zMin + (float)x).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16.0F, zMin + (float)x).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16, zMin + (float)x).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16, zMin + (float)x).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int x = sectionPos.minBlockX() - 16; x <= sectionPos.minBlockX() + 32; x += 2) {
			final float xOffset = (float)((double)x - pCamX);
			final int color = x % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin + 16.0F, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin + 16.0F, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int y = 2; y < 16; y += 2) {
			final int color = y % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin + 16, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin + 16, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin, zMin + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin + 16, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)y, yMin + 16, zMin + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int y = 2; y < 16; y += 2) {
			final int color = y % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zMin + (float)y).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zMin + (float)y).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16, zMin + (float)y).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16, zMin + (float)y).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin, zMin + (float)y).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin, zMin + (float)y).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16, zMin + (float)y).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16, zMin + (float)y).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int y = sectionPos.minBlockY() - 16; y <= sectionPos.minBlockY() + 32; y += 2) {
			final float yOffset = (float)((double)y - pCamY);
			final int color = y % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yOffset, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yOffset, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int z = 2; z < 16; z += 2) {
			final int color = z % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin, zMin + 16).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin, zMin + 16).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin + 16.0F, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin + 16.0F, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin + 16, zMin + 16.0F).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + (float)z, yMin + 16, zMin + 16.0F).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int z = 2; z < 16; z += 2) {
			final int color = z % 4 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)z, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)z, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)z, zMin + 16).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + (float)z, zMin + 16).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + (float)z, zMin).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + (float)z, zMin).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + (float)z, zMin + 16).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + (float)z, zMin + 16).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		for (int z = sectionPos.minBlockZ() - 16; z <= sectionPos.minBlockZ() + 32; z += 2) {
			final float zOffset = (float)((double)z - pCamZ);
			final int color = z % 8 == 0 ? CELL_BORDER : YELLOW;
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(1.0F, 1.0F, 0.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16.0F, zOffset).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16.0F, zOffset).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin, zOffset).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(color);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(1.0F, 1.0F, 0.0F, 0.0F);
		}
		//Blue Lines
		vertexConsumer = pBuffer.getBuffer(RenderType.debugLineStrip(2.0));
		for (int x = -16; x <= 16; x += 16) {
			final int xOffset = x == 16 ? 0 : 16;
			for (int y = -16; y <= 16; y += 16) {
				final int yOffset = y == 16 ? 0 : 16;
				for (int z = -16; z <= 16; z += 16) {
					final int zOffset = z == 16 ? 0 : 16;
					vertexConsumer.addVertex(matrix4f, x, yMin + (float)y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, x, yMin + (float)y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, x + xOffset, yMin + (float)y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, x + xOffset, yMin + (float)y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, y, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, y + yOffset, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, y + yOffset, zMin + (float)z).setColor(0.25F, 0.25F, 1.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, z).setColor(0.25F, 0.25F, 1.0F, 0.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, z).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, z + zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
					vertexConsumer.addVertex(matrix4f, xMin + (float)x, yMin + (float)y, z + zOffset).setColor(0.25F, 0.25F, 1.0F, 0.0F);
				}
			}
		}
		for (int x = sectionPos.minBlockX() - 16; x <= sectionPos.minBlockX() + 32; x += 16) {
			float xOffset = (float)((double)x - pCamX);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(0.25F, 0.25F, 1.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin + 16.0F, zMin + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin + 16.0F, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xOffset, yMin, zMin).setColor(0.25F, 0.25F, 1.0F, 0.0F);
		}
		for (int y = sectionPos.minBlockY() - 16; y <= sectionPos.minBlockY() + 32; y += 16) {
			float yOffset = (float)((double)y - pCamY);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(0.25F, 0.25F, 1.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yOffset, zMin + 16.0F).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yOffset, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yOffset, zMin).setColor(0.25F, 0.25F, 1.0F, 0.0F);
		}
		for (int y = sectionPos.minBlockZ() - 16; y <= sectionPos.minBlockZ() + 32; y += 16) {
			float zOffset = (float)((double)y - pCamZ);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(0.25F, 0.25F, 1.0F, 0.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin + 16.0F, zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin + 16.0F, zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin + 16.0F, yMin, zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(0.25F, 0.25F, 1.0F, 1.0F);
			vertexConsumer.addVertex(matrix4f, xMin, yMin, zOffset).setColor(0.25F, 0.25F, 1.0F, 0.0F);
		}
		pCallbackInfo.cancel();
	}
}
