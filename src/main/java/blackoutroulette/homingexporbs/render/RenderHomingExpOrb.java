package blackoutroulette.homingexporbs.render;

import java.util.Iterator;
import javax.vecmath.Vector4d;
import blackoutroulette.homingexporbs.Constants;
import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.particles.ParticleRainbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderXPOrb;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHomingExpOrb extends RenderXPOrb {

	public static final ParticleRainbow.Factory FACTORY = new ParticleRainbow.Factory();

	protected RenderHomingExpOrb(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityXPOrb en, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(en, x, y - 0.2F, z, entityYaw, partialTicks);
		final EntityHomingExpOrb e = (EntityHomingExpOrb) en;
		final double currentTick = e.updateStep + partialTicks;

		final Iterator<Vector4d> it = e.queuedParticles.iterator();
		while (it.hasNext()) {
			final Vector4d v = it.next();
			if (v.w > currentTick) {
				break;
			}
			final Particle p = FACTORY.createParticle(0, e.world, v.x, v.y, v.z, 0, 0, 0);
			Minecraft.getMinecraft().effectRenderer.addEffect(p);
			it.remove();
		}
	}

	@Override
	public boolean shouldRender(EntityXPOrb e, ICamera camera, double camX, double camY, double camZ) {
		AxisAlignedBB bb = e.getRenderBoundingBox();

		return ((EntityHomingExpOrb) e).isActive() && camera.isBoundingBoxInFrustum(bb)
				&& e.getDistance(camX, camY, camZ) <= Constants.HOMING_RANGE;
	}

}
