package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import net.minecraft.block.Block;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class WardingData extends BlockSavable implements IBlockHook {

    public int owner = 0;

    public WardingData(){}

    public WardingData(WorldCoord coordinates){
        super(coordinates);
    }

    @Override
    public String[] hookMethods(Block block) {
        return new String[0];
    }

    @Override
    public Block getBlock(String method) {
        return null;
    }
}
