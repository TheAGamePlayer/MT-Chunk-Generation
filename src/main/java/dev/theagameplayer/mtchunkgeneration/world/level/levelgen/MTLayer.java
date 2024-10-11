package dev.theagameplayer.mtchunkgeneration.world.level.levelgen;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import dev.theagameplayer.mtchunkgeneration.config.MTCGConfigValues;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

public final class MTLayer {
	private static final ForkJoinPool THREAD_POOL = new ForkJoinPool(16);
	private final NoiseBasedChunkGenerator generator;
	private boolean doCaching = true;
	private int biomeStep, biomeChunkSectionsPerThread, noiseStep, noiseChunkSectionsPerThread;

	public MTLayer(final NoiseBasedChunkGenerator pGenerator) {
		this.generator = pGenerator;
	}
	
	protected final void cacheValues(final int pSectionsCount) {
		this.biomeStep = Math.min(MTCGConfigValues.server.biomeChunkSectionsPerThread, pSectionsCount);
		this.noiseStep = Math.min(MTCGConfigValues.server.noiseChunkSectionsPerThread, pSectionsCount);
		final int biomeHeight = 16 * this.biomeStep;
		final int noiseHeight = 16 * this.noiseStep;
		this.biomeChunkSectionsPerThread = Math.max((this.generator.settings.value().noiseSettings().height() + biomeHeight - 1)/biomeHeight, 1);
		this.noiseChunkSectionsPerThread = Math.max((this.generator.settings.value().noiseSettings().height() + noiseHeight - 1)/noiseHeight, 1);
		this.doCaching = false;
	}

	public final CompletableFuture<ChunkAccess> createBiomes(final RandomState pRandomState, final Blender pBlender, final StructureManager pStructureManager, final ChunkAccess pChunk) {
		if (this.doCaching) this.cacheValues(pChunk.getSectionsCount());
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
			this.doCreateBiomes(pBlender, pRandomState, pStructureManager, pChunk);
			return pChunk;
		}), THREAD_POOL);
	}

	protected final void doCreateBiomes(final Blender pBlender, final RandomState pRandom, final StructureManager pStructureManager, final ChunkAccess pChunk) {
		final NoiseChunk noiseChunk = pChunk.getOrCreateNoiseChunk(chunk -> this.generator.createNoiseChunk(chunk, pStructureManager, pBlender, pRandom));
		final BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(pBlender.getBiomeResolver(this.generator.biomeSource), pChunk);
		final Climate.Sampler sampler = noiseChunk.cachedClimateSampler(pRandom.router(), this.generator.settings.value().spawnTarget());
		final ChunkPos chunkpos = pChunk.getPos();
		final int qx = QuartPos.fromBlock(chunkpos.getMinBlockX());
		final int qz = QuartPos.fromBlock(chunkpos.getMinBlockZ());
		final LevelHeightAccessor levelHeightAccessor = pChunk.getHeightAccessorForGeneration();
		IntStream.range(0, this.biomeChunkSectionsPerThread).parallel().forEach(yStep -> {
			for (int ySec = levelHeightAccessor.getMinSection() + this.biomeStep * yStep, maxYSec = Math.min(levelHeightAccessor.getMaxSection() + 1, levelHeightAccessor.getMinSection() + this.biomeStep * (yStep + 1)); ySec < maxYSec; ++ySec) {
				final LevelChunkSection levelChunkSection = pChunk.getSection(pChunk.getSectionIndexFromSectionY(ySec));
				final int qy = QuartPos.fromSection(ySec);
				levelChunkSection.fillBiomesFromNoise(biomeResolver, sampler, qx, qy, qz);
			}
		});
	}

	public final CompletableFuture<ChunkAccess> fillFromNoise(final Blender pBlender, final RandomState pRandomState, final StructureManager pStructureManager, final ChunkAccess pChunk) {
		if (this.doCaching) this.cacheValues(pChunk.getSectionsCount());
		final NoiseSettings noiseSettings = this.generator.settings.value().noiseSettings().clampToHeightAccessor(pChunk.getHeightAccessorForGeneration());
		final int minY = noiseSettings.minY();
		final int minCellY = Mth.floorDiv(minY, noiseSettings.getCellHeight());
		final int cellCount = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());
		return pChunk.getSectionsCount() == 0 ? CompletableFuture.completedFuture(pChunk) : CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> {
			final int maxCellIndex = pChunk.getSectionIndex(cellCount * noiseSettings.getCellHeight() - 1 + minY);
			final int minCellIndex = pChunk.getSectionIndex(minY);
			final HashSet<LevelChunkSection> set = new HashSet<>();
			for (int cell = maxCellIndex; cell >= minCellIndex; --cell) {
				final LevelChunkSection levelChunkSection = pChunk.getSection(cell);
				levelChunkSection.acquire();
				set.add(levelChunkSection);
			}
			ChunkAccess chunkAccess;
			try {
				chunkAccess = this.doFill(pBlender, pStructureManager, pRandomState, pChunk, minCellY, cellCount);
			} finally {
				for (final LevelChunkSection levelChunkSection : set)
					levelChunkSection.release();
			}
			return chunkAccess;
		}), THREAD_POOL);
	}

	protected final ChunkAccess doFill(final Blender pBlender, final StructureManager pStructureManager, final RandomState pRandom, final ChunkAccess pChunk, final int pMinCellY, int pCellCountY) {
		final NoiseGeneratorSettings noiseGenSettings = this.generator.settings.value();
		final Heightmap heightMap = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		final Heightmap heightMap1 = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		final ChunkPos chunkPos = pChunk.getPos();
		final int minChunkX = chunkPos.getMinBlockX();
		final int minChunkZ = chunkPos.getMinBlockZ();
		IntStream.range(0, this.noiseChunkSectionsPerThread).parallel().forEach(yStep -> {
			final NoiseChunk noiseChunk = this.generator.createNoiseChunk(pChunk, pStructureManager, pBlender, pRandom);
			final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			final Aquifer aquifer = noiseChunk.aquifer();
			final int cellWidth = noiseChunk.cellWidth();
			final int cellHeight = noiseChunk.cellHeight();
			final int cellXZW = 16/cellWidth;
			final int cellYH = 16/cellHeight;
			noiseChunk.initializeForFirstCellX();
			for (int xw = 0; xw < cellXZW; ++xw) {
				final int xv = minChunkX + xw * cellWidth;
				noiseChunk.advanceCellX(xw);
				for (int zw = 0; zw < cellXZW; ++zw) {
					final int zv = minChunkZ + zw * cellWidth;
					for (int cell = pCellCountY - this.noiseStep * yStep * cellYH - 1, minYCell = Math.max(pMinCellY, pCellCountY - this.noiseStep * (yStep + 1) * cellYH) - 1; cell > minYCell; --cell) {
						final int yv = (pMinCellY + cell) * cellHeight;
						final LevelChunkSection levelChunkSection = pChunk.getSection(pChunk.getSectionIndex(yv));
						noiseChunk.selectCellYZ(cell, zw);
						for (int cy = cellHeight - 1; cy > -1; --cy) {
							final int y = yv + cy;
							final int chunkY = y & 15;
							noiseChunk.updateForY(y, (double)cy/(double)cellHeight);
							for (int cx = 0; cx < cellWidth; ++cx) {
								final int x = xv + cx;
								final int chunkX = x & 15;
								noiseChunk.updateForX(x, (double)cx/(double)cellWidth);
								for (int cz = 0; cz < cellWidth; ++cz) {
									final int z = zv + cz;
									final int chunkZ = z & 15;
									noiseChunk.updateForZ(z, (double)cz/(double)cellWidth);
									BlockState blockState = noiseChunk.getInterpolatedState();
									if (blockState == null) blockState = noiseGenSettings.defaultBlock();
									blockState = this.generator.debugPreliminarySurfaceLevel(noiseChunk, x, y, z, blockState);
									if (!SharedConstants.debugVoidTerrain(pChunk.getPos())) {
										levelChunkSection.setBlockState(chunkX, chunkY, chunkZ, blockState, false);
										heightMap.update(chunkX, y, chunkZ, blockState);
										heightMap1.update(chunkX, y, chunkZ, blockState);
										if (aquifer.shouldScheduleFluidUpdate() && !blockState.getFluidState().isEmpty()) {
											mutableBlockPos.set(x, y, z);
											pChunk.markPosForPostprocessing(mutableBlockPos);
										}
									}
								}
							}
						}
					}
				}
				noiseChunk.swapSlices();
			}
			noiseChunk.stopInterpolation();
		});
		return pChunk;
	}
}
