package blackoutroulette.homingexporbs.entitys;

import blackoutroulette.homingexporbs.Constants;
import blackoutroulette.homingexporbs.HomingExpOrbs;
import blackoutroulette.homingexporbs.math.Vec3d;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityHomingExpOrb extends EntityXPOrb implements IEntityAdditionalSpawnData {

	public Vec3d lastParticlePos = null;
	protected EntityPlayer target = null;
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
		this.setNoGravity(true);
		this.noClip = true;

		setSize(Constants.SIZE, Constants.SIZE);
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

		if(this.target == null){
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;

			this.target = world.getClosestPlayerToEntity(this, HomingExpOrbs.config.homingRange);

			if(this.target == null || this.target.isSpectator()){
				this.target = null;
			}else {
				Vec3d velocity = getTargetPos();

				velocity.sub(getPos());
				final float distance = (float) velocity.length();
				final float speedMul = distance > Constants.ORB_MAX_SPEED_DISTANCE? 1 : distance / Constants.ORB_MAX_SPEED_DISTANCE;

				final float angleToXAxis = (float) velocity.angleXZPlane();
				// point the vector in the direction of the negative z-axis
				velocity = new Vec3d(0, 0, -Constants.MAX_VELOCITY * speedMul);
				// rotate between 45° and 135°
				velocity.rotX(this.launchAngle);
				// rotate the x-axis to the target
				velocity.rotY(angleToXAxis);

				setVelocity(velocity);
			}
		}else{
			calculateVelocityVector();
		}

		this.move();
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

	public void move(){
		this.setPosition(this.posX + motionX, this.posY + motionY, this.posZ + motionZ);
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
		final float w = this.width / 2.0F;
		final float h = this.height / 2.0F;
		this.setEntityBoundingBox(new AxisAlignedBB(x - w, y - h, z - w, x + w, y + h, z + w));
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

	public Entity getTarget(){
		return this.target;
	}

	protected Vec3d getTargetPos(){
		return getEntityPos(target);
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
