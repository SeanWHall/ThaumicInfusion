package drunkmafia.thaumicinfusion.common.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;

import java.util.Map;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@IFMLLoadingPlugin.Name(value = ModInfo.MODID)
@IFMLLoadingPlugin.MCVersion(value = "1.7.10")
public class ThaumicInfusionPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() { return null; }
}
