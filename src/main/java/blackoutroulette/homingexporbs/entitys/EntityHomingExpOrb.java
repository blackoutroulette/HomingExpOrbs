package blackoutroulette.homingexporbs.entitys;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
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
	protected static final HashMap<String, MutablePair<Long, Integer>> PLAYER_DELAY_MAP = new HashMap<String, MutablePair<Long, Integer>>();
	public final LinkedList<Vector4d> queuedParticles = new LinkedList<>();
	public static final Random RNG = new Random();

	protected EntityPlayer closestPlayer;
	protected Vector3d target;
	protected Vector3d velocity = new Vector3d();
	protected boolean targetIsPlayer = false;
	protected Delay delay = new Delay();
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

		delay.decrease();
		if (closestPlayer == null && !delay.hasDelay()) {
			if (playerInRange()) {
				createDummyTarget();
			} else {
				// check again in 20 ticks (1 second)
				delay.add(20);
				return false;
			}
		}

		setActive(!delay.hasDelay() && closestPlayer != null);
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

		createParticles();
	}

	protected void createParticles() {
		if (!world.isRemote || queuedParticles == null) {
			return;
		}

		int emissionRate;
		final int ps = Minecraft.getMinecraft().gameSettings.particleSetting;
		if (ps == 0) {
			emissionRate = Constants.MAX_PARTICLE_ALL;
		} else if (ps == 1) {
			emissionRate = Constants.MAX_PARTICLE_DEC;
		} else {
			return;
		}

		final Vector3d lastPos = new Vector3d(prevPosX, prevPosY, prevPosZ);
		final Vector3d dif = new Vector3d(posX, posY, posZ);
		dif.sub(lastPos);
		for (int i = 0; i < emissionRate; ++i) {
			final Vector3d v = new Vector3d(dif);
			final double partialTick = i * (1.0D / emissionRate);
			v.scale(partialTick);
			v.add(lastPos);
			final Vector4d vec = new Vector4d(v);
			vec.w = this.updateStep + partialTick;
			queuedParticles.add(vec);
		}
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
		delay.add(10);
	}

	protected boolean playerInRange() {
		if (closestPlayer == null || closestPlayer.isSpectator()
				|| closestPlayer.getDistance(this) > Constants.HOMING_RANGE) {
			closestPlayer = world.getClosestPlayerToEntity(this, Constants.HOMING_RANGE);
			if (closestPlayer == null || closestPlayer.isSpectator()) {
				closestPlayer = null;
				return false;
			}
			setSpawnDelay();
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

		final double scale = Math.max(0.5D, closestPlayer.getDistance(this) / (Constants.HOMING_RANGE / 2));
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

	/**
	 * Lets orbs fly with a slight delay to each other based on player. For
	 * esthetics only.
	 */
	protected void setSpawnDelay() {
		final String username = closestPlayer.getName();
		final long worldTime = world.getTotalWorldTime();

		MutablePair<Long, Integer> pair = PLAYER_DELAY_MAP.get(username);
		if (pair == null) {
			pair = new MutablePair<>(worldTime, 0);
			PLAYER_DELAY_MAP.put(username, pair);
		}

		final int i = Constants.MAX_SPAWN_DELAY / 2;
		final int rand = EntityHomingExpOrb.RNG.nextInt(Constants.MAX_SPAWN_DELAY - i) + i;
		pair.setRight(Math.max(0, pair.getRight() + rand - (int) (worldTime - pair.getLeft())));
		pair.setLeft(worldTime);

		delay.add(pair.getRight());
	}

	/**
	 * Safety class to avoid logic errors.
	 */
	class Delay {
		private int delay = 0;

		public boolean hasDelay() {
			return this.delay > 0;
		}

		public void add(int delay) {
			this.delay += Math.abs(delay);
		}

		public void decrease() {
			if (this.delay > 0) {
				--this.delay;
			}
		}
	}
}
