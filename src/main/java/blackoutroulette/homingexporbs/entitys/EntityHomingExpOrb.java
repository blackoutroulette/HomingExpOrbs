package main.java.blackoutroulette.homingexporbs.entitys;

import java.util.HashMap;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3d;

import org.apache.commons.lang3.tuple.MutablePair;

import main.java.blackoutroulette.homingexporbs.Constants;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityHomingExpOrb extends EntityXPOrb {

	// server-only values
	protected static final HashMap<String, MutablePair<Long, Integer>> PLAYER_DELAY_MAP = new HashMap<String, MutablePair<Long, Integer>>();
	public static final Random RNG = new Random();
	private static boolean rand;

	protected EntityPlayer closestPlayer;
	protected Vector3d offset;
	protected Vector3d target;
	protected Vector3d velocity = new Vector3d();
	protected boolean targetIsPlayer;
	protected Delay delay = new Delay();

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
		offset = new Vector3d((double) RNG.nextInt(5) / 10.0D, -((double) RNG.nextInt(5) / 10.0D),
				(double) RNG.nextInt(5) / 10.0D);
	}

	@Override
	public void onUpdate() {
		if (world.isRemote) {
			updateClient();
			return;
		}

		if (!preconditions()) {
			return;
		}

		calculateTrajectory();

		if (super.xpOrbAge >= Constants.MAX_LIFETIME) {
			setDead();
		}
	}

	protected void updateClient() {
		if (isActive()) {
			++super.xpColor;
		}
	}

	/**
	 * Checks if the orb should be updated
	 * 
	 * @return true if a player in range exists and no delay is set.
	 */
	protected boolean preconditions() {
		++super.xpOrbAge;
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
		Vector3d v3;
		if (targetIsPlayer) {
			v3 = new Vector3d(closestPlayer.posX,
					closestPlayer.posY + (double) this.closestPlayer.getEyeHeight() / 2.0D, closestPlayer.posZ);
			v3.add(offset);
		} else {
			v3 = new Vector3d(target);
		}

		v3.sub(pos);
		final double distance = v3.length();

		if (!targetIsPlayer && distance <= 0.5D) {
			targetIsPlayer = true;
		}

		v3.normalize();
		v3.scale(Constants.MAX_VELOCITY);

		// steering
		v3.sub(velocity);
		v3.clampMax(Constants.MAX_VELOCITY);
		v3.scale(1.0F / Constants.MASS);
		velocity.add(v3);
		velocity.clampMax(Constants.MAX_VELOCITY);
		setPosition(posX += velocity.x, posY += velocity.y, posZ += velocity.z);
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
		targetIsPlayer = false;
		final Vector3d pos = new Vector3d(posX, posY, posZ);
		target = new Vector3d(pos);
		target.normalize();
		final int r = getRandomInt();
		target.x = target.z * r;
		target.y = RNG.nextFloat() + 1;
		target.z = target.x * -r;

		final double scale = Math.min(1.0D, closestPlayer.getDistance(posX, posY, posZ) / 32.0D);
		target.scale(scale);
		target.add(pos);
	}

	/**
	 * Distributes values evenly.
	 * 
	 * @return 1 if last return was -1 else -1.
	 */
	protected int getRandomInt() {
		final int i = rand ? 1 : -1;
		rand = !rand;
		return i;
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

		int i = Constants.MAX_SPAWN_DELAY / 2;
		int rand = EntityHomingExpOrb.RNG.nextInt(Constants.MAX_SPAWN_DELAY - i) + i;
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
