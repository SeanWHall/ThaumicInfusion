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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = ("spiritus"), cost = 4)
public class Spiritus extends AspectEffect {

    private IBlockState oldState;
    private TileEntity oldTile;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (world.getBlockState(pos.pos).getBlock() == TIBlocks.fakeAirBlock)
            world.setBlockState(pos.pos, Blocks.air.getDefaultState());

        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos blockPos, IBlockState state, Random random) {
        if (world.isRemote)
            return;

        world.scheduleUpdate(blockPos, state.getBlock(), 1);

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(blockPos.getX() - 0.1D, blockPos.getY() - 0.1D, blockPos.getZ() - 0.1D, blockPos.getX() + 1.1D, blockPos.getY() + 1.1D, blockPos.getZ() + 1.1D);
        if (world.getEntitiesWithinAABB(EntityPlayer.class, bb).size() > 0) {
            if (oldState == null) {
                if (state.getBlock().getMobilityFlag() == 0 && state.getBlock() != Blocks.bedrock && state.getBlock().getBlockHardness(world, blockPos) != -1.0F) {
                    oldState = world.getBlockState(blockPos);
                    oldTile = world.getTileEntity(blockPos);
                    if (oldTile != null) world.removeTileEntity(blockPos);
                    world.setBlockState(blockPos, TIBlocks.fakeAirBlock.getDefaultState());
                    world.scheduleUpdate(blockPos, TIBlocks.fakeAirBlock, 1);
                }
            }
        } else if (oldState != null) {
            world.setBlockState(blockPos, oldState);
            if (oldTile != null) {
                oldTile.validate();
                world.setTileEntity(blockPos, oldTile);
            }

            world.scheduleUpdate(blockPos, state.getBlock(), 1);
            oldState = null;
            oldTile = null;
        }

    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        updateTick(world, pos, state, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entityIn) {
        updateTick(world, pos, world.getBlockState(pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        updateTick(world, pos, state, world.rand);
    }
}
