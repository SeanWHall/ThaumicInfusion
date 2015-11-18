/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = ("permutatio"), cost = 4)
public class Permutatio extends AspectLink {

    private boolean lastRedstoneSignal;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        WorldCoordinates destin = getDestination();
        if (destin == null) {
            world.scheduleUpdate(pos, state.getBlock(), 1);
            return;
        }

        World destinationWorld = DimensionManager.getWorld(destin.dim);
        boolean power = world.isBlockIndirectlyGettingPowered(pos) > 1;
        if (power != lastRedstoneSignal) {
            lastRedstoneSignal = power;

            IBlockState newBlock = destinationWorld.getBlockState(destin.pos);
            TileEntity oldTile = world.getTileEntity(pos), newTile = destinationWorld.getTileEntity(destin.pos);

            destinationWorld.removeTileEntity(destin.pos);
            world.removeTileEntity(pos);

            destinationWorld.setBlockState(destin.pos, Blocks.air.getDefaultState());
            destinationWorld.setBlockState(destin.pos, state);

            world.setBlockState(pos, Blocks.air.getDefaultState());
            world.setBlockState(pos, newBlock);

            destinationWorld.forceBlockUpdateTick(newBlock.getBlock(), destin.pos, world.rand);

            if (oldTile != null) {
                destinationWorld.removeTileEntity(destin.pos);
                oldTile.validate();
                destinationWorld.setTileEntity(destin.pos, oldTile);
            }

            if (newTile != null) {
                world.removeTileEntity(pos);
                newTile.validate();
                world.setTileEntity(pos, newTile);
            }
        }
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entityIn) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, world.rand);
    }
}
