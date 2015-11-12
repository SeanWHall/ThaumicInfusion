/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "permutatio")
public class Permutatio extends AspectLink {

    private boolean lastRedstoneSignal;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @Override
    public int getCost() {
        return 8;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote) return;
        WorldCoordinates destin = this.getDestination();
        if (destin == null) {
            world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
            return;
        }

        World destinationWorld = DimensionManager.getWorld(destin.dim);
        boolean power = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (power != this.lastRedstoneSignal) {
            this.lastRedstoneSignal = power;

            Block oldBlock = world.getBlock(x, y, z), newBlock = destinationWorld.getBlock(destin.x, destin.y, destin.z);
            TileEntity oldTile = world.getTileEntity(x, y, z), newTile = destinationWorld.getTileEntity(destin.x, destin.y, destin.z);

            int oldMeta = world.getBlockMetadata(x, y, z), newMeta = destinationWorld.getBlockMetadata(destin.x, destin.y, destin.z);

            destinationWorld.removeTileEntity(destin.x, destin.y, destin.z);
            world.removeTileEntity(x, y, z);

            destinationWorld.setBlock(destin.x, destin.y, destin.z, Blocks.air);
            destinationWorld.setBlock(destin.x, destin.y, destin.z, oldBlock, oldMeta, 3);

            world.setBlock(x, y, z, Blocks.air);
            world.setBlock(x, y, z, newBlock, newMeta, 3);

            destinationWorld.scheduleBlockUpdate(destin.x, destin.y, destin.z, oldBlock, 1);

            if (oldTile != null) {
                destinationWorld.removeTileEntity(destin.x, destin.y, destin.z);
                oldTile.validate();
                destinationWorld.setTileEntity(destin.x, destin.y, destin.z, oldTile);
            }

            if (newTile != null) {
                world.removeTileEntity(x, y, z);
                newTile.validate();
                world.setTileEntity(x, y, z, newTile);
            }
        }
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
