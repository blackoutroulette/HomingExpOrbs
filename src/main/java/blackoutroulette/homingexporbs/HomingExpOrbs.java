package blackoutroulette.homingexporbs;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.render.RenderHomingExpOrbFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class HomingExpOrbs {
	@Instance
	public static HomingExpOrbs instance;

	public static ConfigHandler config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new ConfigHandler();

		EntityRegistry.registerModEntity(new ResourceLocation(Constants.MODID + ":" + "homingexporb"),
				EntityHomingExpOrb.class, "homingexporb", 0, HomingExpOrbs.instance, HomingExpOrbs.config.homingRange, 1, true);

		if (event.getSide() == Side.CLIENT) {
			RenderingRegistry.registerEntityRenderingHandler(EntityHomingExpOrb.class,
					RenderHomingExpOrbFactory.INSTANCE);
		}
	}
}
