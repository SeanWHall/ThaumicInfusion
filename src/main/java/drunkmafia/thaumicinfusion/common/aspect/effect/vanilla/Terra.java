package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EntitySyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "terra", cost = 2)
public class Terra extends AspectEffect{

    @Override
    public void updateBlock(World world) {
        if(world.isRemote)
            return;

        WorldCoord pos = getPos();
        if (!world.isAirBlock(pos.x, pos.y, pos.z) && world.isAirBlock(pos.x, pos.y - 1, pos.z)) {
            InfusedBlockFalling entity = new InfusedBlockFalling(world, (double) ((float) pos.x + 0.5F), (double) ((float) pos.y + 0.5F), (double) ((float) pos.z + 0.5F), Block.getIdFromBlock(world.getBlock(pos.x, pos.y, pos.z)), world.getBlockMetadata(pos.x, pos.y, pos.z), world.getTileEntity(pos.x, pos.y, pos.z));

            world.removeTileEntity(pos.x, pos.y, pos.z);
            world.setBlock(pos.x, pos.y, pos.z, Blocks.air);

            world.spawnEntityInWorld(entity);
            ChannelHandler.network.sendToDimension(new EntitySyncPacketC(entity), world.provider.dimensionId);
        }
    }
}
