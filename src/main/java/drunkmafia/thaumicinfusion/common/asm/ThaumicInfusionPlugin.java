/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

@IFMLLoadingPlugin.Name(ModInfo.MODID)
@IFMLLoadingPlugin.TransformerExclusions({ "drunkmafia.thaumicinfusion.common.asm.", "drunkmafia.thaumicinfusion.common.aspect", "java."})
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ThaumicInfusionPlugin implements IFMLLoadingPlugin {

    public static Logger log = LogManager.getLogger("TI Transformer");
    public static PrintWriter logger;
    public static boolean isObf;

    public static String block, world, iBlockAccess;

    public ThaumicInfusionPlugin() throws FileNotFoundException, UnsupportedEncodingException {
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            isObf = !deobfuscatedEnvironment.getBoolean(null);

            logger = new PrintWriter("TI_Transformer.log", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Thaumic Infusion has detected an " + (isObf ? "Obfuscated" : "Deobfuscated") + " environment!");
        block = isObf ? "aji" : "net/minecraft/block/Block";
        world = isObf ? "ahb" : "net/minecraft/world/World";
        iBlockAccess = isObf ? "ahl" : "net/minecraft/world/IBlockAccess";

        log.info("Block: " + block);
        log.info("World: " + world);
        log.info("Access: " + iBlockAccess);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{BlockTransformer.class.getName(), WorldTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {

        return null;
    }

    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
