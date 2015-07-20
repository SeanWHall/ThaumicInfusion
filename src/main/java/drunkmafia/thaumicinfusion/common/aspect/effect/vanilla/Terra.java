/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EntitySyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "terra", cost = 2)
public class Terra extends AspectEffect{

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);

        WorldCoordinates pos = getPos();
        if (!world.isAirBlock(pos.x, pos.y, pos.z) && world.isAirBlock(pos.x, pos.y - 1, pos.z)) {
            InfusedBlockFalling entity = new InfusedBlockFalling(world, (double) ((float) pos.x + 0.5F), (double) ((float) pos.y + 0.5F), (double) ((float) pos.z + 0.5F), Block.getIdFromBlock(world.getBlock(pos.x, pos.y, pos.z)), world.getBlockMetadata(pos.x, pos.y, pos.z), world.getTileEntity(pos.x, pos.y, pos.z));

            world.removeTileEntity(pos.x, pos.y, pos.z);
            world.setBlock(pos.x, pos.y, pos.z, Blocks.air);

            world.spawnEntityInWorld(entity);
            ChannelHandler.instance().sendToDimension(new EntitySyncPacketC(entity), world.provider.dimensionId);
        }
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        return false;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
