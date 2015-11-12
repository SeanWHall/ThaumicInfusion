/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.lib;

public class ModInfo {

    /* Base Mod Info */

    public static final String MODID = "thaumicinfusion";
    public static final String NAME = "Thaumic Infusion";
    public static final String VERSION = "4.3";
    public static final String CHANNEL = ModInfo.MODID.toUpperCase();

    /* Classpaths */

    public static final String BASE_PATH = "drunkmafia.thaumicinfusion";

    public static final String CLIENT_PATH = ModInfo.BASE_PATH + ".client";
    public static final String CLIENT_PROXY_PATH = ModInfo.CLIENT_PATH + ".ClientProxy";

    public static final String COMMON_PATH = ModInfo.BASE_PATH + ".common";
    public static final String COMMON_PROXY_PATH = ModInfo.COMMON_PATH + ".CommonProxy";

    /* Additional Information */

    public static final String CREATIVETAB_UNLOCAL = "thaumicinfusion";
}
