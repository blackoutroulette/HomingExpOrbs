package blackoutroulette.homingexporbs;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.render.RenderHomingExpOrbFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION)
public class HomingExpOrbs {
	@Instance
	public static HomingExpOrbs instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation(Constants.MODID + ":" + "homingexporb"),
				EntityHomingExpOrb.class, "homingexporb", 0, HomingExpOrbs.instance, Constants.HOMING_RANGE, 1, true);

		if (event.getSide() == Side.CLIENT) {
			RenderingRegistry.registerEntityRenderingHandler(EntityHomingExpOrb.class,
					RenderHomingExpOrbFactory.INSTANCE);
		}
	}
}
