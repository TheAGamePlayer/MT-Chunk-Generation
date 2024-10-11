package dev.theagameplayer.mtchunkgeneration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.theagameplayer.mtchunkgeneration.config.MTCGConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

//TheAGamePlayer was here :>
@Mod(MTChunkGenerationMod.MODID)
public final class MTChunkGenerationMod {
	public static final String MODID = "mtchunkgeneration";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public MTChunkGenerationMod(final ModContainer pModContainer, final IEventBus pModEventBus) {
		this.createConfig(pModContainer, pModEventBus);
	}
	
	private final void createConfig(final ModContainer pModContainer, final IEventBus pModEventBus) {
		final boolean flag = FMLEnvironment.dist.isClient();
		MTCGConfig.initConfig(pModContainer, flag);
		if (flag) pModContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		pModEventBus.addListener(MTCGConfig::loading);
		pModEventBus.addListener(MTCGConfig::reloading);
		LOGGER.info("Created mod config.");
	}
	
	public static final ResourceLocation namespace(final String pName) {
		return ResourceLocation.fromNamespaceAndPath(MODID, pName);
	}
}
