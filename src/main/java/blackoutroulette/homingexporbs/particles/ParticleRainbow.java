package blackoutroulette.homingexporbs.particles;

import java.awt.Color;

import blackoutroulette.homingexporbs.Constants;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleRainbow extends ParticleSpell {

	public static final int baseTextureIndex = 151;

	protected ParticleRainbow(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i1229_8_,
			double ySpeed, double p_i1229_12_) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i1229_8_, ySpeed, p_i1229_12_);
		this.setParticleTextureIndex(baseTextureIndex);
		this.setMaxAge(Constants.PARTICLE_LIFETIME);
		setColor();
	}

	protected void setColor() {
		final float hue = ((float)Math.sin(world.getWorldTime() * Constants.COLOR_STEP) + 1F) / 2F;
		final Color c = Color.getHSBColor(hue, 1.0F, 1.0F);
		setRBGColorF(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F);
		setAlphaF(Constants.COLOR_ALPHA);
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.setParticleTextureIndex(baseTextureIndex - (int) (this.particleMaxAge / 8 * this.particleAge));

		this.particleAge++;
		if (this.particleAge >= this.particleMaxAge) {
			this.setExpired();
		}
	}

	@Override
	public boolean shouldDisableDepth() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {

		@Override
		public Particle createParticle(int particleID, World w, double x, double y, double z, double xSpeed,
				double ySpeed, double zSpeed, int... opt) {
			return new ParticleRainbow(w, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}
