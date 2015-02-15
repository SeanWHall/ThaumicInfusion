package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("potentia"), cost = 4)
public class Potentia  extends AspectEffect {

    public void onBlockAdded(World world, int x, int y, int z){
        world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
        world.notifyBlocksOfNeighborChange(x, y + 1, z, this);
        world.notifyBlocksOfNeighborChange(x - 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x + 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x, y, z - 1, this);
        world.notifyBlocksOfNeighborChange(x, y, z + 1, this);
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
        world.notifyBlocksOfNeighborChange(x, y + 1, z, this);
        world.notifyBlocksOfNeighborChange(x - 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x + 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x, y, z - 1, this);
        world.notifyBlocksOfNeighborChange(x, y, z + 1, this);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
        world.notifyBlocksOfNeighborChange(x, y + 1, z, this);
        world.notifyBlocksOfNeighborChange(x - 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x + 1, y, z, this);
        world.notifyBlocksOfNeighborChange(x, y, z - 1, this);
        world.notifyBlocksOfNeighborChange(x, y, z + 1, this);
    }

    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int p_149709_5_){
        int i1 = access.getBlockMetadata(x, y, z);
        return i1 == 5 && p_149709_5_ == 1 ? 0 : (i1 == 3 && p_149709_5_ == 3 ? 0 : (i1 == 4 && p_149709_5_ == 2 ? 0 : (i1 == 1 && p_149709_5_ == 5 ? 0 : (i1 == 2 && p_149709_5_ == 4 ? 0 : 15))));
    }

    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side){
        return side == 0 ? this.isProvidingWeakPower(access, x, y, z, side) : 0;
    }
}
