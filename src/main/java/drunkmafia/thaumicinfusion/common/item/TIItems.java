package drunkmafia.thaumicinfusion.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.item.Item;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIItems {

    public static Item focusInfusing;
    public static Item focusWarding;

    public static void init(){
        focusInfusing = (new ItemFocusInfusing()).setUnlocalizedName("FocusInfusion");
        GameRegistry.registerItem(focusInfusing, "FocusInfusion", ModInfo.MODID);
    }
}
