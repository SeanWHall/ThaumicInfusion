/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = "aer", cost = 2)
public class Aer extends AspectEffect {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote) checkIfAir(world, pos.pos);
    }

    @Override
    public void onRemoveEffect() {
        World world = ChannelHandler.getServerWorld(pos.dim);
        if (world != null && world.getBlockState(pos.pos).getBlock() == TIBlocks.fakeAirWithCollisionBlock)
            world.setBlockToAir(pos.pos);
    }

    private void checkIfAir(World world, BlockPos pos) {
        if (world.isAirBlock(pos))
            world.setBlockState(pos, TIBlocks.fakeAirWithCollisionBlock.getDefaultState());
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) checkIfAir(worldIn, pos);
    }
}
