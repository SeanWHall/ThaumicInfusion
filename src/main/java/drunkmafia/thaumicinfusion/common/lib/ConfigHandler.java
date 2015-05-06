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

    public static boolean debug;

    public static long maxTimeout;

    public static void init(File configFile){
        config = new Configuration(configFile);
        config.load();
        
        maxTimeout = config.get("Networking", "Packet Timeout", 10000, "How many times a single block can send a packet per update, the lower the numbers, the faster an infused or essentia block will be update however can cause lag").getInt();
        //debug = config.get("Misc", "Debug", false, "Debug mode prints information to the log file").getBoolean();

        config.save();
    }
}
