package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = "gelum", cost = 1)
public class Gelum extends AspectEffect implements IServerTickable {

    private IBlockState oldState;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (world.getBlockState(pos.pos).getBlock() == TIBlocks.fakeAirBlock)
            world.setBlockState(pos.pos, Blocks.air.getDefaultState());
    }

    private IBlockState getStateForMaterial(Material material) {
        return material == Material.water ? Blocks.ice.getDefaultState() : Blocks.cobblestone.getDefaultState();
    }

    @Override
    public void serverTick(World world) {
        BlockPos blockPos = pos.pos;
        IBlockState state = world.getBlockState(blockPos);

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), blockPos.getX() + 1D, blockPos.getY() + 2D, blockPos.getZ() + 1D);
        if (world.getEntitiesWithinAABB(EntityPlayer.class, bb).size() > 0) {
            if (oldState == null && state.getBlock().getMaterial() instanceof MaterialLiquid) {
                oldState = world.getBlockState(blockPos);
                world.setBlockState(blockPos, getStateForMaterial(state.getBlock().getMaterial()));
            }
        } else if (oldState != null) {
            world.setBlockState(blockPos, oldState);
            oldState = null;
        }
    }
}
