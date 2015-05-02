package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.block.Block;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public interface IBlockHook {
    public String[] hookMethods();
    public Block getBlock(String method);
}
