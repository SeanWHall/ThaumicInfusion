/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.block.Block;

public interface IBlockHook {
    String[] hookMethods(Block block);
    Block getBlock(String method);

    boolean shouldOverride(String method);
}
