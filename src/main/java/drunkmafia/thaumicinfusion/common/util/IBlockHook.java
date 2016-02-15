/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.block.Block;

public interface IBlockHook {
    //int[] hookMethods(Block block);

    Block getBlock(int method);

    boolean shouldOverride(int method);
}
