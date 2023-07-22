package blackoutroulette.homingexporbs;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler extends Configuration {

    private static ConfigHandler INSTANCE = null;

    private static final String filename = "config/HomingExpOrbs.cfg";


    protected boolean particlesDisabled;
    protected int homingMaxRange;

    protected ConfigHandler() {
        super(new File(filename), Constants.VERSION);

        homingMaxRange = this.getInt("homingMaxRange", "defaults", 64, 1, 256, "Maximal range in which orbs will target players");
        particlesDisabled = this.getBoolean("particlesDisabled", "defaults", false, "Disables the orb particle trail");

        this.save();
    }

    public static ConfigHandler getInstance(){
        if(ConfigHandler.INSTANCE == null){
            ConfigHandler.INSTANCE = new ConfigHandler();
        }
        return ConfigHandler.INSTANCE;
    }

    public boolean isParticlesDisabled() {
        return particlesDisabled;
    }

    public void setParticlesDisabled(boolean particlesDisabled) {
        this.particlesDisabled = particlesDisabled;
    }

    public int getHomingMaxRange() {
        return homingMaxRange;
    }

    public void setHomingMaxRange(int homingMaxRange) {
        this.homingMaxRange = homingMaxRange;
    }
}