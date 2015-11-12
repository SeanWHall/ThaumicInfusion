/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

@Name(ModInfo.MODID)
@TransformerExclusions({"drunkmafia.thaumicinfusion.common.asm.", "drunkmafia.thaumicinfusion.common.aspect"})
@MCVersion("1.7.10")
public class ThaumicInfusionPlugin implements IFMLLoadingPlugin {

    public static Logger log = LogManager.getLogger("TI Transformer");
    public static PrintWriter logger;
    public static boolean isObf;

    public static String block, world, iBlockAccess;

    public ThaumicInfusionPlugin() {
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            ThaumicInfusionPlugin.isObf = !deobfuscatedEnvironment.getBoolean(null);

            ThaumicInfusionPlugin.logger = new PrintWriter("TI_Transformer.log", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ThaumicInfusionPlugin.log.info("Thaumic Infusion has detected an " + (ThaumicInfusionPlugin.isObf ? "Obfuscated" : "Deobfuscated") + " environment!");
        ThaumicInfusionPlugin.block = ThaumicInfusionPlugin.isObf ? "aji" : "net/minecraft/block/Block";
        ThaumicInfusionPlugin.world = ThaumicInfusionPlugin.isObf ? "ahb" : "net/minecraft/world/World";
        ThaumicInfusionPlugin.iBlockAccess = ThaumicInfusionPlugin.isObf ? "ahl" : "net/minecraft/world/IBlockAccess";
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
