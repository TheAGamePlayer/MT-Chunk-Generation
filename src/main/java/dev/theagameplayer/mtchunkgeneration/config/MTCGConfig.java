package dev.theagameplayer.mtchunkgeneration.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;

import dev.theagameplayer.mtchunkgeneration.MTChunkGenerationMod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MTCGConfig {
	private static final Logger LOGGER = MTChunkGenerationMod.LOGGER;
	private static final String CONFIG = MTChunkGenerationMod.MODID + ".config.";
	protected static final ServerConfig SERVER = new ServerConfig();
	protected static final ClientConfig CLIENT = new ClientConfig();
	
	public static final class ServerConfig {
		private final ModConfigSpec spec;
		public final ModConfigSpec.IntValue biomeChunkSectionsPerThread;
		public final ModConfigSpec.IntValue noiseChunkSectionsPerThread;
		
		private ServerConfig() {
			final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			this.biomeChunkSectionsPerThread = builder
					.translation(CONFIG + "biome_chunk_sections_per_thread")
					.worldRestart()
					.comment("Determines how many chunk sections each thread generates for biome generation.")
					.defineInRange("biomeChunkSectionsPerThread", 8, 1, Integer.MAX_VALUE);
			this.noiseChunkSectionsPerThread = builder
					.translation(CONFIG + "noise_chunk_sections_per_thread")
					.worldRestart()
					.comment("Determines how many chunk sections each thread generates for noise generation.")
					.defineInRange("noiseChunkSectionsPerThread", 8, 1, Integer.MAX_VALUE);
			this.spec = builder.build();
		}
	}
	
	public static final class ClientConfig {
		protected final ModConfigSpec spec;
		public final ModConfigSpec.BooleanValue use3dChunkDebugging;
		public final ModConfigSpec.BooleanValue useMTGBanners;
		
		private ClientConfig() {
			final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
			this.use3dChunkDebugging = builder
					.translation(CONFIG + "use_3d_chunk_debugging")
					.comment("Enables 3D chunk debugging.")
					.define("use3dChunkDebugging", true);
			this.useMTGBanners = builder
					.translation(CONFIG + "use_mtg_banners")
					.comment("Enables MTG banners.")
					.define("useMTGBanners", true);
			this.spec = builder.build();
		}
	}
	
	public static final void initConfig(final ModContainer pModContainer, final boolean pIsClient) {
		final Path configPath = FMLPaths.CONFIGDIR.get();
		final Path mtcgConfigPath = Paths.get(configPath.toAbsolutePath().toString(), MTChunkGenerationMod.MODID);
		try {
			Files.createDirectory(mtcgConfigPath);
		} catch (final FileAlreadyExistsException exception) {
			LOGGER.info("Config directory for " + MTChunkGenerationMod.MODID + " already exists!");
		} catch (final IOException exception) {
			LOGGER.error("Failed to create " + MTChunkGenerationMod.MODID + " config directory!", exception);
		}
		pModContainer.registerConfig(ModConfig.Type.SERVER, SERVER.spec, mtcgConfigPath.resolve(MTChunkGenerationMod.MODID + "-server.toml").toString());
		if (pIsClient) pModContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT.spec, mtcgConfigPath.resolve(MTChunkGenerationMod.MODID + "-client.toml").toString());
	}
	
	public static final void loading(final ModConfigEvent.Loading pEvent) {
		sync(pEvent.getConfig());
	}
	
	public static final void reloading(final ModConfigEvent.Reloading pEvent) {
		sync(pEvent.getConfig());
	}
	
	private static final void sync(final ModConfig pConfig) {
		switch (pConfig.getType()) {
		case SERVER:
			if (!pConfig.getSpec().equals(MTCGConfig.SERVER.spec)) return;
			MTCGConfigValues.syncServer();
			return;
		case CLIENT:
			if (!pConfig.getSpec().equals(MTCGConfig.CLIENT.spec)) return;
			MTCGConfigValues.syncClient();
			return;
		default: return;
		}
	}
}
