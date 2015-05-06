package drunkmafia.thaumicinfusion.common.lib;

public class ModInfo {

    /* Base Mod Info */

    public static final String MODID = "thaumicinfusion";
    public static final String NAME = "Thaumic Infusion";
    public static final String VERSION = "4";
    public static final String CHANNEL = MODID.toUpperCase();

    /* Classpaths */

    public static final String BASE_PATH = "drunkmafia.thaumicinfusion";

    public static final String CLIENT_PATH = BASE_PATH + ".client";
    public static final String CLIENT_PROXY_PATH = CLIENT_PATH + ".ClientProxy";

    public static final String COMMON_PATH = BASE_PATH + ".common";
    public static final String COMMON_PROXY_PATH = COMMON_PATH + ".CommonProxy";
    public static final String COMMON_EFFET_PATH = COMMON_PATH + ".aspect.effect.vanilla";

    /* JAVA Error Messages */

    public static final String ASPECT_EFFECT_REGISTER_ERROR = "Aspect Effect failed to register";

    /* Additional Information */

    public static final String CREATIVETAB_UNLOCAL = "thaumicinfusion";
    public static final String EXTRA_INFO_XML = "https://dl.dropboxusercontent.com/u/101919880/Thaumic%20Infusion/ThaumicInfusion.xml";
}
