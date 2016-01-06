/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TIItems {

    public static Item focusInfusing;
    public static Item coordinatePaper;

    public static void preInit() {
        TIItems.focusInfusing = new ItemFocusInfusing().setUnlocalizedName("focus_infusion");
        TIItems.coordinatePaper = new ItemCoordinatePaper().setUnlocalizedName("coordinate_paper");

        GameRegistry.registerItem(TIItems.focusInfusing, "focus_infusion");
        GameRegistry.registerItem(TIItems.coordinatePaper, "coordinate_paper");
    }
}
