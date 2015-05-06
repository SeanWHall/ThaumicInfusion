package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.block.Block;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public interface IBlockHook {
    String[] hookMethods(Block block);

    Block getBlock(String method);

    /**
     * TODO - Add the ability to chose if the effect should stop the orginal block code from running
     * boolean shouldReturn(String method);
     */
}
