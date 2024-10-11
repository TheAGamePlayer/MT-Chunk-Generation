package dev.theagameplayer.mtchunkgeneration.mixin;

import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.theagameplayer.mtchunkgeneration.world.level.levelgen.MTLayer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

@Mixin(NoiseBasedChunkGenerator.class)
public final class NoiseBasedChunkGeneratorMixin {
	private final MTLayer layer = new MTLayer((NoiseBasedChunkGenerator)(Object)this);
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;createBiomes(Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;", cancellable = true)
	private final void createBiomes(final RandomState pRandomState, final Blender pBlender, final StructureManager pStructureManager, final ChunkAccess pChunk, final CallbackInfoReturnable<CompletableFuture<ChunkAccess>> pCallbackInfo) {
		pCallbackInfo.setReturnValue(this.layer.createBiomes(pRandomState, pBlender, pStructureManager, pChunk));
	}
	
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;fillFromNoise(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;", cancellable = true)
	private final void fillFromNoise(final Blender pBlender, final RandomState pRandomState, final StructureManager pStructureManager, final ChunkAccess pChunk, final CallbackInfoReturnable<CompletableFuture<ChunkAccess>> pCallbackInfo) {
		pCallbackInfo.setReturnValue(this.layer.fillFromNoise(pBlender, pRandomState, pStructureManager, pChunk));
	}
}
