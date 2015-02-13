package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EntitySyncPacketC;
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
        if(world.isAirBlock(pos.x, pos.y - 1, pos.z)){
            InfusedBlockFalling entity = new InfusedBlockFalling(world, (double)((float)pos.x + 0.5F), (double)((float)pos.y + 0.5F), (double)((float)pos.z + 0.5F), data, world.getBlockMetadata(pos.x, pos.y, pos.z));
            world.spawnEntityInWorld(entity);
            TIWorldData.getWorldData(world).removeBlock(pos, true);

            ChannelHandler.network.sendToDimension(new EntitySyncPacketC(entity), world.provider.dimensionId);
        }
    }
}
