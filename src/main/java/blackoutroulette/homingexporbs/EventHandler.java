package main.java.blackoutroulette.homingexporbs;

import main.java.blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityXpOrbSpawn(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		World w = event.getWorld();
		if (w.isRemote || e == null || e.world.isRemote || e.getClass() != EntityXPOrb.class) {
			return;
		}

		w.spawnEntity(new EntityHomingExpOrb((EntityXPOrb) e));
		event.setCanceled(true);
	}
}
