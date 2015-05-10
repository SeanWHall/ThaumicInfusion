package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by DrunkMafia on 08/10/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("permutatio"), cost = 4)
public class Permutatio extends AspectLink {

    private boolean lastRedstoneSignal;

    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote) return;

        WorldCoord pos = getPos();
        World destinationWorld = getDestinationWorld();
        boolean power = world.isBlockIndirectlyGettingPowered(pos.x, pos.y, pos.z);
        if (power != lastRedstoneSignal) {
            WorldCoord destin = getDestination();
            if (destin == null)
                return;


            Block oldBlock = world.getBlock(pos.x, pos.y, pos.z), newBlock = destinationWorld.getBlock(destin.x, destin.y, destin.z);
            TileEntity oldTile = world.getTileEntity(pos.x, pos.y, pos.z), newTile = destinationWorld.getTileEntity(destin.x, destin.y, destin.z);

            int oldMeta = world.getBlockMetadata(pos.x, pos.y, pos.z), newMeta = destinationWorld.getBlockMetadata(destin.x, destin.y, destin.z);

            destinationWorld.removeTileEntity(destin.x, destin.y, destin.z);
            world.removeTileEntity(pos.x, pos.y, pos.z);

            destinationWorld.setBlock(destin.x, destin.y, destin.z, Blocks.air);
            destinationWorld.setBlock(destin.x, destin.y, destin.z, oldBlock, oldMeta, 3);

            world.setBlock(pos.x, pos.y, pos.z, Blocks.air);
            world.setBlock(pos.x, pos.y, pos.z, newBlock, newMeta, 3);

            if (oldTile != null) {
                destinationWorld.removeTileEntity(destin.x, destin.y, destin.z);
                oldTile.validate();
                destinationWorld.setTileEntity(destin.x, destin.y, destin.z, oldTile);
            }

            if (newTile != null) {
                world.removeTileEntity(pos.x, pos.y, pos.z);
                newTile.validate();
                world.setTileEntity(pos.x, pos.y, pos.z, newTile);
            }

            lastRedstoneSignal = power;
        }
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setByte("lastRedstoneSignal", (byte) (lastRedstoneSignal ? 1 : 0));
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        lastRedstoneSignal = tagCompound.getByte("lastRedstoneSignal") == 1;
    }
}
