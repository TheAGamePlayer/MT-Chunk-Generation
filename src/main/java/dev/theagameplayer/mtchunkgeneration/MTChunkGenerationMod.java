package dev.theagameplayer.mtchunkgeneration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.mtchunkgeneration.config.MTCGConfig;
import dev.theagameplayer.mtchunkgeneration.event.MTCGPlayerEvents;
import dev.theagameplayer.mtchunkgeneration.registries.other.MTCGPackets;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

//TheAGamePlayer was here :>
@Mod(MTChunkGenerationMod.MODID)
public final class MTChunkGenerationMod {
	public static final String MODID = "mtchunkgeneration";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public MTChunkGenerationMod(final ModContainer pModContainer, final IEventBus pModEventBus) {
		this.createConfig(pModContainer, pModEventBus);
		attachCommonEventListeners(pModEventBus, NeoForge.EVENT_BUS);
	}
	
	public static final ResourceLocation namespace(final String pName) {
		return ResourceLocation.fromNamespaceAndPath(MODID, pName);
	}
	
	private final void createConfig(final ModContainer pModContainer, final IEventBus pModEventBus) {
		final boolean flag = FMLEnvironment.dist.isClient();
		MTCGConfig.initConfig(pModContainer, flag);
		if (flag) pModContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		pModEventBus.addListener(MTCGConfig::loading);
		pModEventBus.addListener(MTCGConfig::reloading);
		LOGGER.info("Created mod config.");
	}
	
	public static final void attachCommonEventListeners(final IEventBus pModBus, final IEventBus pForgeBus) {
		//Registries
		pModBus.addListener(MTCGPackets::registerPackets);
		//Player
		pForgeBus.addListener(MTCGPlayerEvents::loggedIn);
		pForgeBus.addListener(MTCGPlayerEvents::playerRespawn);
		pForgeBus.addListener(MTCGPlayerEvents::changedDimension);
	}
}
