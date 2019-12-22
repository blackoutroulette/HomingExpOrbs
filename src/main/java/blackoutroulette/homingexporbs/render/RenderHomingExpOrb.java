package main.java.blackoutroulette.homingexporbs.render;

import java.awt.Color;

import main.java.blackoutroulette.homingexporbs.Constants;
import main.java.blackoutroulette.homingexporbs.entitys.EntityHomingExpOrb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderXPOrb;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;

public class RenderHomingExpOrb extends RenderXPOrb {

	protected static final ParticleSpell.WitchFactory f = new ParticleSpell.WitchFactory();

	protected RenderHomingExpOrb(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityXPOrb e, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(e, x, y, z, entityYaw, partialTicks);

		// this is apparently minecraft's way
		// to determine if a particle should be spawned or not
		// reference: RenderGlobal.spawnParticle0()
		int ps = Minecraft.getMinecraft().gameSettings.particleSetting * 4 + 1;
		if (EntityHomingExpOrb.RNG.nextInt(ps) != 0) {
			return;
		}

		double posX = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks;
		double posY = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks;
		double posZ = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks;

		Particle p = f.createParticle(0, e.world, posX, posY + 0.2D, posZ, 0, 0, 0);
		float hue = (e.world.getWorldTime() % (1.0F / Constants.COLOR_STEP)) * Constants.COLOR_STEP;
		Color c = Color.getHSBColor(hue, 1.0F, 1.0F);
		p.setRBGColorF(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F);
		p.setAlphaF(Constants.COLOR_ALPHA);
		Minecraft.getMinecraft().effectRenderer.addEffect(p);
	}

	@Override
	public boolean shouldRender(EntityXPOrb e, ICamera camera, double camX, double camY, double camZ) {
		AxisAlignedBB bb = e.getRenderBoundingBox();

		return ((EntityHomingExpOrb) e).isActive() && camera.isBoundingBoxInFrustum(bb)
				&& e.getDistance(camX, camY, camZ) <= Constants.HOMING_RANGE;
	}

}
