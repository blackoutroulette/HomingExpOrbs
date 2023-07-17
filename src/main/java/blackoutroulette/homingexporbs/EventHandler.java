package blackoutroulette.homingexporbs;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onEntityXpOrbSpawn(EntityJoinWorldEvent event) {
		final Entity e = event.getEntity();
		if (e.getClass() != EntityXPOrb.class) {
			return;
		}

		e.setDead();
		event.setCanceled(true);

		final EntityHomingExpOrb heo = new EntityHomingExpOrb(e.world, e.posX, e.posY, e.posZ, ((EntityXPOrb) e).xpValue);
		event.getWorld().spawnEntity(heo);
	}
}
