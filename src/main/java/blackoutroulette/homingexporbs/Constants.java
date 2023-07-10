package blackoutroulette.homingexporbs;

public final class Constants {
	public static final String MODID = "homingexporbs";
	public static final String NAME = "Homing Expierence Orbs";
	public static final String VERSION = "1.1";

	// Entity values
	public static final double MAX_VELOCITY = 1.5D;
	public static final double MASS = 8.0D;
	public static final float SIZE = 0.25F;
	public static final int MAX_SPAWN_DELAY = 20;
	public static final int MAX_LIFETIME = 6000;
	public static final int MIN_ANGLE = 45;
	public static final int MAX_ANGLE = 135;

	// Render values
	public static final float COLOR_CYCLE_TICKS = 50; // 20 ticks = 1sec
	public static final float COLOR_ALPHA = 0.7F;
	public static final int PARTICLE_LIFETIME = 8;
	public static final int[] PARTICLE_DISTANCE = {1, 3};
}
