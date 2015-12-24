/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

@IFMLLoadingPlugin.Name(ModInfo.MODID)
@IFMLLoadingPlugin.TransformerExclusions({"drunkmafia.thaumicinfusion.common.asm.", "drunkmafia.thaumicinfusion.common.aspect"})
@IFMLLoadingPlugin.MCVersion("1.8")
public class ThaumicInfusionPlugin implements IFMLLoadingPlugin {

    public static Logger log = LogManager.getLogger("TI Transformer");
    public static PrintWriter logger;
    public static boolean isObf;

    public static String block, blockPos, world, iBlockAccess;

    public ThaumicInfusionPlugin() {
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            ThaumicInfusionPlugin.isObf = !deobfuscatedEnvironment.getBoolean(null);

            ThaumicInfusionPlugin.logger = new PrintWriter("TI_Transformer.log", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Thaumic Infusion has detected an " + (ThaumicInfusionPlugin.isObf ? "Obfuscated" : "Deobfuscated") + " environment!");
        block = ThaumicInfusionPlugin.isObf ? "atr" : "net/minecraft/block/Block";
        blockPos = ThaumicInfusionPlugin.isObf ? "dt" : "net/minecraft/util/BlockPos";
        world = ThaumicInfusionPlugin.isObf ? "aqu" : "net/minecraft/world/World";
        iBlockAccess = ThaumicInfusionPlugin.isObf ? "ard" : "net/minecraft/world/IBlockAccess";
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
