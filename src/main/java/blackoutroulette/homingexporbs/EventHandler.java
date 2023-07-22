package blackoutroulette.homingexporbs;

import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.network.ConfigMessage;
import blackoutroulette.homingexporbs.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

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

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerLoggedInEvent event){
		// as long as you are on the server you can safely cast any EntityPlayer to EntityPlayerMP
		final EntityPlayerMP player = (EntityPlayerMP) event.player;

		// send server config to new joined player
		final ConfigHandler cfg = ConfigHandler.getInstance();
		final ConfigMessage cfgMsg = new ConfigMessage(cfg.homingMaxRange);
		PacketHandler.INSTANCE.sendTo(cfgMsg, player);
	}
}
