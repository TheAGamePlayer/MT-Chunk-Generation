package dev.theagameplayer.mtchunkgeneration.config;

import net.neoforged.fml.loading.FMLEnvironment;

public final class MTCGConfigValues {
	public static ServerValues server;
	public static ClientValues client;
	
	public static final void syncServer() {
		server = new ServerValues();
	}

	public static final void syncClient() {
		if (FMLEnvironment.dist.isClient()) client = new ClientValues();
	}

	public static final class ServerValues {
		public final int biomeChunkSectionsPerThread = MTCGConfig.SERVER.biomeChunkSectionsPerThread.get();
		public final int noiseChunkSectionsPerThread = MTCGConfig.SERVER.noiseChunkSectionsPerThread.get();
	}
	
	public static final class ClientValues {
		public final boolean use3dChunkDebugging = MTCGConfig.CLIENT.use3dChunkDebugging.get();
	}
}
