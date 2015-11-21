/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "terra", cost = 2)
public class Terra extends AspectEffect {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (BlockFalling.canFallInto(world, pos.down()) && pos.getY() >= 0) {
            if (world.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                if (!world.isRemote && state.getBlock().getMaterial().getMaterialMobility() > 0) {
                    EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, (double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, world.getBlockState(pos));
                    TileEntity tileEntity = world.getTileEntity(pos);

                    if (tileEntity != null) {
                        NBTTagCompound tileTag = new NBTTagCompound();
                        tileEntity.writeToNBT(tileTag);
                        entityfallingblock.tileEntityData = tileTag;
                    }

                    world.spawnEntityInWorld(entityfallingblock);
                }
            }
        }

        world.scheduleUpdate(pos, this, 4);
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
