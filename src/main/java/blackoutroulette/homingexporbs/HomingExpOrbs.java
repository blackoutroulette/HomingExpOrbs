package blackoutroulette.homingexporbs;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.network.ConfigMessage;
import blackoutroulette.homingexporbs.network.PacketHandler;
import blackoutroulette.homingexporbs.render.RenderHomingExpOrbFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class HomingExpOrbs {
	@Instance
	public static HomingExpOrbs instance;
	public static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// init logging
		logger = event.getModLog();

		// load config
		final int trackingRange = ConfigHandler.getInstance().getHomingMaxRange();

		// register custom entity
		EntityRegistry.registerModEntity(new ResourceLocation(Constants.MODID + ":" + Constants.ENTITY_NAME),
				EntityHomingExpOrb.class, Constants.ENTITY_NAME, 0, HomingExpOrbs.instance, trackingRange, 1, true);

		// register custom render
		RenderingRegistry.registerEntityRenderingHandler(EntityHomingExpOrb.class, RenderHomingExpOrbFactory.INSTANCE);

		// register network
		PacketHandler.INSTANCE.registerMessage(ConfigMessage.ConfigMessageHandler.class, ConfigMessage.class, PacketHandler.id++, Side.CLIENT);
	}
}
