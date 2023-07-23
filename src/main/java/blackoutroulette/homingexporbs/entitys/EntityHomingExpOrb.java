package blackoutroulette.homingexporbs.entitys;

import blackoutroulette.homingexporbs.ConfigHandler;
import blackoutroulette.homingexporbs.Constants;
import blackoutroulette.homingexporbs.math.Vec3d;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.UUID;

public class EntityHomingExpOrb extends EntityXPOrb implements IEntityAdditionalSpawnData {


	public Vec3d lastParticlePos;

	static final DataParameter<Optional<UUID>> TARGET_ID = EntityDataManager.<Optional<UUID>>createKey(EntityHomingExpOrb.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	protected Optional<UUID> lastTarget;

	protected int delay;
	protected float launchAngle;

	public EntityHomingExpOrb(World w, double x, double y, double z, int xpValue) {
		super(w, x, y, z, xpValue);
		this.delay = this.rand.nextInt(Constants.MAX_SPAWN_DELAY);
		// launch angle between 45° and 135°
		this.launchAngle = ((this.rand.nextFloat() * Vec3d.PI / 2F) + Vec3d.PI / 4F);
	}

	public EntityHomingExpOrb(World w) {
		super(w);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(TARGET_ID, Optional.absent());
		this.lastTarget = Optional.absent();
		this.lastParticlePos = null;
		this.setNoGravity(true);
		this.noClip = true;
		setSize(Constants.SIZE, Constants.SIZE);
		updateBoundingBox();
	}

	@Override
	public void onUpdate() {
		++super.xpOrbAge;
		++super.xpColor;
		if (super.xpOrbAge >= Constants.MAX_LIFETIME) {
			setDead();
			return;
		}

		if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA)
		{
			this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
			this.setDead();
			return;
		}

		if(this.delay > 0){
			--this.delay;
			return;
		}

		final Optional<UUID> id = this.dataManager.get(TARGET_ID);
		if(!id.isPresent()){
			findClosestPlayer();
			return;
		}

		// will desync if the target player is not present on the client
		final EntityPlayer target = this.world.getPlayerEntityByUUID(id.get());
		if(target == null){
			findClosestPlayer();
			return;
		}

		final int dist = (int) target.getDistance(this);
		final ConfigHandler cfg = ConfigHandler.getInstance();
		if(dist > cfg.getHomingMaxRange()){
			findClosestPlayer();
			return;
		}

		if(!lastTarget.isPresent()) {
			Vec3d velocity = getTargetPos();

			velocity.sub(getPos());
			final float distance = (float) velocity.length();
			final float speedMul = distance > Constants.ORB_MAX_SPEED_DISTANCE ? 1 : distance / Constants.ORB_MAX_SPEED_DISTANCE;

			final float angleToXAxis = (float) velocity.angleXZPlane();
			// point the vector in the direction of the negative z-axis
			velocity = new Vec3d(0, 0, -Constants.MAX_VELOCITY * speedMul);
			// rotate between 45° and 135°
			velocity.rotX(this.launchAngle);
			// rotate the x-axis to the target
			velocity.rotY(angleToXAxis);

			setVelocity(velocity);

		}else{
			calculateVelocityVector();
		}

		this.lastTarget = id;
		super.move(MoverType.SELF, motionX, motionY, motionZ);
	}

	protected void findClosestPlayer() {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if(!this.world.isRemote){
			final EntityPlayer closestPlayer = this.world.getClosestPlayerToEntity(this, ConfigHandler.getInstance().getHomingMaxRange());
			final Optional<UUID> opt = closestPlayer == null? Optional.absent() : Optional.of(closestPlayer.getUniqueID());
			this.dataManager.set(TARGET_ID, opt);
		}
	}

	protected void calculateVelocityVector() {
		final Vec3d pos = getPos();
		Vec3d v = getTargetPos();
		v.sub(pos);

		v.normalize();
		v.mul(Constants.MAX_VELOCITY);

		// steering
		Vec3d velocity = getVelocity();
		v.sub(velocity);
		v.div(Constants.MASS);
		velocity.add(v);
		if(velocity.length() > Constants.MAX_VELOCITY) {
			velocity.normalize();
			velocity.mul(Constants.MAX_VELOCITY);
		}
		this.setVelocity(velocity);
	}

	protected void updateBoundingBox() {
		final double d = this.width / 2.0D;
		final double d1 = this.height / 2.0D;
		this.setEntityBoundingBox(
				new AxisAlignedBB(
				this.posX - d,
				this.posY - d1,
				this.posZ - d,
				this.posX + d,
				this.posY + d1,
				this.posZ + d
				)
		);
	}

	protected void updatePitchAndYaw() {
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;

		final Vec3d vel = getVelocity();
		this.rotationPitch = (float) Math.toDegrees(Math.asin(-vel.y));
		this.rotationYaw = (float) Math.toDegrees(-Math.atan2(vel.x, vel.z));
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		entityIn.xpCooldown = 0;
		delayBeforeCanPickup = 0;
		super.onCollideWithPlayer(entityIn);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean handleWaterMovement() {
		return false;
	}

	public Vec3d getPos(){
		return getEntityPos(this);
	}

	public EntityPlayer getTarget(){
		final Optional<UUID> id = this.dataManager.get(TARGET_ID);

		if (!id.isPresent()) {
			return null;
		}

		return this.world.getPlayerEntityByUUID(id.get());
	}

	protected Vec3d getTargetPos(){
		return getEntityPos(getTarget());
	}

	public static Vec3d getEntityPos(Entity e) {
		return new Vec3d(e.posX, e.posY, e.posZ);
	}

	public Vec3d getVelocity() {
		return new Vec3d(motionX, motionY, motionZ);
	}

	public void setVelocity(Vec3d v){
		this.motionX = v.x;
		this.motionY = v.y;
		this.motionZ = v.z;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(this.delay);
		buffer.writeFloat(this.launchAngle);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		this.delay = additionalData.readInt();
		this.launchAngle = additionalData.readFloat();
	}
}
