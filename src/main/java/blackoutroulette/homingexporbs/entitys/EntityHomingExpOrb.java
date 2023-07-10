package blackoutroulette.homingexporbs.entitys;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import blackoutroulette.homingexporbs.HomingExpOrbs;
import org.apache.commons.lang3.tuple.MutablePair;
import blackoutroulette.homingexporbs.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityHomingExpOrb extends EntityXPOrb {

	// server-only values
	// Todo: use custom rng with min distance between values
	public static final Random RNG = new Random();


	public Vector3d lastParticlePos = null;
	protected EntityPlayer closestPlayer;
	protected Vector3d target;
	protected Vector3d velocity = new Vector3d();
	protected boolean targetIsPlayer = false;
	protected int delay = 0;
	public int updateStep = 0;

	// synchronized values
	protected static final DataParameter<Boolean> ACTIVE = EntityDataManager.createKey(EntityHomingExpOrb.class,
			DataSerializers.BOOLEAN);

	public EntityHomingExpOrb(EntityXPOrb orb) {
		super(orb.world, orb.posX, orb.posY, orb.posZ, orb.xpValue);
		orb.setDead();
	}

	public EntityHomingExpOrb(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		dataManager.register(ACTIVE, false);
		this.setNoGravity(true);
		this.noClip = true;

		setSize(Constants.SIZE, Constants.SIZE);
	}

	@Override
	public void onUpdate() {
		if (world.isRemote && isActive()) {
			++super.xpColor;
			return;
		}

		if (!preconditions()) {
			return;
		}

		calculateTrajectory();
	}

	/**
	 * Checks if the orb should be updated
	 * 
	 * @return true if a player in range exists and no delay is set.
	 */
	protected boolean preconditions() {
		++super.xpOrbAge;
		if (super.xpOrbAge >= Constants.MAX_LIFETIME) {
			setDead();
			return false;
		}

		--delay;
		if (closestPlayer == null && delay <= 0) {
			if (playerInRange()) {
				createDummyTarget();
			} else {
				// check again in 20 ticks (1 second)
				delay += 20;
				return false;
			}
		}

		setActive(delay <= 0 && closestPlayer != null);
		return isActive();
	}

	protected void calculateTrajectory() {
		final Vector3d pos = new Vector3d(posX, posY, posZ);
		final Vector3d v = getTarget();
		v.sub(pos);

		if (!targetIsPlayer && v.length() <= 0.5D) {
			targetIsPlayer = true;
		}

		v.normalize();
		v.scale(Constants.MAX_VELOCITY);

		// steering
		v.sub(velocity);
		v.scale(1.0F / Constants.MASS);
		velocity.add(v);
		if(velocity.length() > Constants.MAX_VELOCITY) {
			velocity.normalize();
			velocity.scale(Constants.MAX_VELOCITY);
		}
		setPosition(posX + velocity.x, posY + velocity.y, posZ + velocity.z);
	}
	
	protected Vector3d getTarget() {
		if(targetIsPlayer) {
			return new Vector3d(closestPlayer.posX,	closestPlayer.posY + this.closestPlayer.getEyeHeight() / 2.0D, closestPlayer.posZ);
		}
		return new Vector3d(target);
	}

	@Override
	public void setPosition(double x, double y, double z) {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		
		updatePitchAndYaw();
		
		// Forge - Process chunk registration after moving.
		if (this.isAddedToWorld() && !this.world.isRemote)
			this.world.updateEntityWithOptionalForce(this, false); 
		
		// bounding box
		final double d = this.width / 2.0D;
		final double d1 = this.height / 2.0D;
		this.setEntityBoundingBox(new AxisAlignedBB(x - d, y - d1, z - d, x + d, y + d1, z + d));

		++this.updateStep;
	}

	protected void updatePitchAndYaw() {
		final Vector3d direction = new Vector3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ);
		direction.normalize();

		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.rotationPitch = (float) Math.toDegrees(-Math.asin(direction.y));
		this.rotationYaw = -((float) MathHelper.atan2(direction.x, direction.z)) * (180F / (float) Math.PI);
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		entityIn.xpCooldown = 0;
		delayBeforeCanPickup = 0;
		super.onCollideWithPlayer(entityIn);
	}

	@Override
	public void readEntityFromNBT(@Nonnull NBTTagCompound cmp) {
		super.readEntityFromNBT(cmp);
		targetIsPlayer = true;
		delay += 10;
	}

	protected boolean playerInRange() {
		if (closestPlayer == null || closestPlayer.isSpectator()
				|| closestPlayer.getDistance(this) > HomingExpOrbs.config.homingRange) {
			closestPlayer = world.getClosestPlayerToEntity(this, HomingExpOrbs.config.homingRange);
			if (closestPlayer == null || closestPlayer.isSpectator()) {
				closestPlayer = null;
				return false;
			}
			delay += EntityHomingExpOrb.RNG.nextInt(Constants.MAX_SPAWN_DELAY);
		}
		return true;
	}

	/**
	 * Creates a dummy target to manipulate the orb trajectory. For esthetics only.
	 */
	protected void createDummyTarget() {
		final Vector2d v = new Vector2d(posX, posZ);
		v.sub(new Vector2d(closestPlayer.posX, closestPlayer.posZ));
		v.normalize();

		final double angle = Math
				.toRadians(RNG.nextInt(Constants.MAX_ANGLE - Constants.MIN_ANGLE) + Constants.MIN_ANGLE);
		final double rotXZ = Math.cos(angle);
		final double rotY = Math.sin(angle);
		v.scale(rotXZ);

		final double scale = Math.max(0.5D, closestPlayer.getDistance(this) / (HomingExpOrbs.config.homingRange / 2F));
		target = new Vector3d(v.y, rotY, -v.x);
		target.scale(scale);
		target.add(new Vector3d(posX, posY, posZ));
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean handleWaterMovement() {
		return false;
	}

	public boolean isActive() {
		return dataManager.get(ACTIVE);
	}

	protected void setActive(boolean b) {
		dataManager.set(ACTIVE, b);
	}

}
