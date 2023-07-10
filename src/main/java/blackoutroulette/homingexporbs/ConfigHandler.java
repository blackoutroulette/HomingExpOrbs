package blackoutroulette.homingexporbs;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries;


import java.io.File;


public class ConfigHandler extends Configuration {

    public static final String filename = "config/HomingExpOrbs.cfg";

    public final boolean disableParticles;
    public final int homingRange;

    ConfigHandler() {
        super(new File(filename), Constants.VERSION);

        homingRange = this.getInt("homingRange", "defaults", 64, 1, 256, "Range in which orbs will target players");
        disableParticles = this.getBoolean("disableParticles", "defaults", false, "Disables the orb particle trail");

        this.save();
    }
}
