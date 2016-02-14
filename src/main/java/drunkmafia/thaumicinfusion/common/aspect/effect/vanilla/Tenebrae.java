package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.BlockWrapper;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "tenebrae", cost = 4)
public class Tenebrae extends AspectEffect {

    private IBlockState nightState = TIBlocks.fakeAirBlock.getDefaultState();
    private boolean placedState, suppressDrop;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote) {
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
            checkIfAir(world, pos.pos);
        }
    }

    @Override
    public void onRemoveEffect() {
        World world = ChannelHandler.getServerWorld(pos.dim);
        if (world != null && world.getBlockState(pos.pos).getBlock() == TIBlocks.fakeAirWithCollisionBlock)
            world.setBlockToAir(pos.pos);
    }

    private void checkIfAir(World world, BlockPos pos) {
        if (world.isAirBlock(pos))
            world.setBlockState(pos, TIBlocks.fakeAirBlock.getDefaultState());
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            checkIfAir(world, pos);
            updateTick(world, pos, state, world.rand);
        }
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos blockPos, IBlockState state, Random random) {
        if (world.isRemote)
            return;

        final IBlockState fakeAir = TIBlocks.fakeAirBlock.getDefaultState();

        if (world.isDaytime()) {
            if (placedState && nightState != fakeAir) {
                world.setBlockState(blockPos, fakeAir);
                placedState = false;
                suppressDrop = true;
            }
        } else {
            if (state == fakeAir) {
                if (nightState != fakeAir) {
                    world.setBlockState(blockPos, nightState);
                    placedState = true;
                    suppressDrop = true;
                }
            } else if (state != nightState && !placedState) {
                nightState = state;
                placedState = true;
            }
        }

        world.scheduleUpdate(blockPos, world.getBlockState(blockPos).getBlock(), 1);
    }

    @Override
    @BlockMethod(overrideBlockFunc = true)
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        //Stops block from dropping as an item if it is about to be removed
        if (suppressDrop) {
            suppressDrop = false;
            return;
        }

        //Allows the original method that invoked this, to run without causing a infinite loop
        BlockWrapper.suppressed = true;
        state.getBlock().dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
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
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if (nightState != TIBlocks.fakeAirBlock.getDefaultState())
            tagCompound.setInteger("nightState", Block.getStateId(nightState));
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.hasKey("nightState"))
            nightState = Block.getStateById(tagCompound.getInteger("nightState"));
    }
}
