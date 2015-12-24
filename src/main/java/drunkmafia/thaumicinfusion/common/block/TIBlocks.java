/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TIBlocks {

    public static Block fakeAirBlock;

    public static void initBlocks() {
        GameRegistry.registerBlock(fakeAirBlock = new FakeAirBlock(), "unlocal_fakeAirBlock");
    }
}
