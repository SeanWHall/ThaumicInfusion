package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.BlockWrapper;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Effect(aspect = "praemunio", cost = 4)
public class Praemunio extends AspectEffect {

    @Override
    @BlockMethod
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        BlockWrapper.suppressed = true;
        AxisAlignedBB bb = state.getBlock().getCollisionBoundingBox(worldIn, pos, state);
        if (bb.maxY < 5.5D) bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, 5.5D, bb.maxZ);
        return bb;
    }
}
