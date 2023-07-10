package blackoutroulette.homingexporbs.render;

import javax.vecmath.Vector3d;

import blackoutroulette.homingexporbs.Constants;
import blackoutroulette.homingexporbs.HomingExpOrbs;
import blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import blackoutroulette.homingexporbs.particles.ParticleRainbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderXPOrb;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHomingExpOrb extends RenderXPOrb {

	protected RenderHomingExpOrb(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityXPOrb en, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(en, x, y - 0.2F, z, entityYaw, partialTicks);
		final EntityHomingExpOrb e = (EntityHomingExpOrb) en;

		if(HomingExpOrbs.config.disableParticles || !e.isActive()){
			return;
		}

		final int ps = Minecraft.getMinecraft().gameSettings.particleSetting;
		if(ps > 1){
			return; // minimal particles case
		}
		final int particleDistance = Constants.PARTICLE_DISTANCE[ps];

		Vector3d lastPos;
		if(e.lastParticlePos == null) {
			lastPos = new Vector3d(e.prevPosX, e.prevPosY, e.prevPosZ);
			e.lastParticlePos = lastPos;
		}else{
			lastPos = e.lastParticlePos;
		}

		final Vector3d currentPos = new Vector3d(e.posX, e.posY, e.posZ);

		Vector3d lastPosDiff = new Vector3d();
		lastPosDiff.sub(currentPos, lastPos);
		lastPosDiff.scale(partialTicks);
		final double positionDist = lastPosDiff.length();
		final int newParticles = (int)(positionDist / particleDistance);
		lastPosDiff.scale(particleDistance / positionDist);

		Vector3d v;
		for (int i = 0; i < newParticles; ++i) {
			v = new Vector3d(lastPosDiff);
			v.scale(i+1);
			v.add(lastPos);
			e.lastParticlePos = v;
			final ParticleRainbow p = new ParticleRainbow(e.world, v.x, v.y, v.z, 0, 0.1F, 0);
			p.setColorFromTicks(e.world.getWorldTime() + partialTicks);
			Minecraft.getMinecraft().effectRenderer.addEffect(p);
		}
	}

	@Override
	public boolean shouldRender(EntityXPOrb e, ICamera camera, double camX, double camY, double camZ) {
		AxisAlignedBB bb = e.getRenderBoundingBox();

		return camera.isBoundingBoxInFrustum(bb) && e.getDistance(camX, camY, camZ) <= HomingExpOrbs.config.homingRange;
	}

}
