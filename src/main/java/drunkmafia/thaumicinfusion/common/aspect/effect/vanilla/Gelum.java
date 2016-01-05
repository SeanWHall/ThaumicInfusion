package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "gelum", cost = 1)
public class Gelum extends AspectEffect {

    public static long cooldownTimer = 10000L;
    private long cooldown;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entityIn) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
        return false;
    }

    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
        if (world.isRemote || System.currentTimeMillis() < cooldown + cooldownTimer)
            return;

        int radius = 10;
        for (int xPos = pos.getX() - radius; xPos < pos.getX() + radius; xPos++) {
            for (int yPos = pos.getY() - radius; yPos < pos.getY() + radius; yPos++) {
                for (int zPos = pos.getZ() - radius; zPos < pos.getZ() + radius; zPos++) {
                    Block block = world.getBlockState(pos).getBlock();

                    if (block != null) {
                        if (block.getMaterial() == Material.water) {
                            world.setBlockState(new BlockPos(xPos, yPos, zPos), Blocks.ice.getDefaultState());
                            cooldown = System.currentTimeMillis();
                        } else if (block != Blocks.snow_layer && world.canBlockSeeSky(new BlockPos(xPos, yPos, zPos)) && world.isAirBlock(new BlockPos(xPos, yPos + 1, zPos))) {
                            world.setBlockState(new BlockPos(xPos, yPos, zPos), Blocks.snow_layer.getDefaultState());
                            cooldown = System.currentTimeMillis();
                        }
                        return;
                    }
                }
            }
        }
        cooldown = System.currentTimeMillis();
    }
}
