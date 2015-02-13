package drunkmafia.thaumicinfusion.common.lib;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public final class ConfigHandler {

    public static Configuration config;

    public static boolean shouldUseSafeTiles, shouldUseIndepthSearch;

    public static long maxTimeout;

    public static void init(File configFile){
        config = new Configuration(configFile);
        config.load();

        shouldUseSafeTiles = config.get("Tileentities", "ShouldUse", true, "This will toggle the ability to infuse tile entities").getBoolean();
        shouldUseIndepthSearch = config.get("Tileentities", "indepthSearch", true, "This will enable the scanning of classes to check for changes, disable for even quicker loading times but may cause crashes when updating mods.").getBoolean();

        maxTimeout = config.get("Networking", "Packet Timeout", 10000, "How many times a single block can send a packet per update, the lower the numbers, the faster an infused or essentia block will be update however can cause lag").getInt();

        config.save();
    }
}
