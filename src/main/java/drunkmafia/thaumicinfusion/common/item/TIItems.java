/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.item;

import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TIItems {

    public static Item focusInfusing;
    public static Item coordinatePaper;

    public static void init() {
        TIItems.focusInfusing = new ItemFocusInfusing().setUnlocalizedName("FocusInfusion");
        TIItems.coordinatePaper = new ItemCoordinatePaper().setUnlocalizedName("CoordinatePaper");

        GameRegistry.registerItem(TIItems.focusInfusing, "FocusInfusion", ModInfo.MODID);
        GameRegistry.registerItem(TIItems.coordinatePaper, "CoordinatePaper", ModInfo.MODID);
    }
}
