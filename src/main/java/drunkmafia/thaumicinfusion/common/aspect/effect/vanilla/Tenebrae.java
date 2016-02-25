package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.BlockWrapper;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = "tenebrae", cost = 4)
public class Tenebrae extends AspectEffect implements IServerTickable {

    private IBlockState nightState = TIBlocks.fakeAirBlock.getDefaultState();
    private boolean placedState, suppressDrop;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote) {
            serverTick(world);
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
            if (!world.isDaytime() && placedState) {
                nightState = TIBlocks.fakeAirBlock.getDefaultState();
                placedState = false;
            }
            serverTick(world);
        }
    }

    @Override
    public void serverTick(World world) {
        if (world.isRemote)
            return;

        BlockPos blockPos = pos.pos;
        IBlockState state = world.getBlockState(blockPos);
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
            } else if (state != Blocks.air) {
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
