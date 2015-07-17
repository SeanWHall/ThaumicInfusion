/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.item.Item;

public class TIItems {

    public static Item focusInfusing;
    public static Item coordinatePaper;

    public static void init(){
        focusInfusing = (new ItemFocusInfusing()).setUnlocalizedName("FocusInfusion");
        coordinatePaper = (new ItemCoordinatePaper()).setUnlocalizedName("CoordinatePaper");

        GameRegistry.registerItem(focusInfusing, "FocusInfusion", ModInfo.MODID);
        GameRegistry.registerItem(coordinatePaper, "CoordinatePaper", ModInfo.MODID);
    }
}
