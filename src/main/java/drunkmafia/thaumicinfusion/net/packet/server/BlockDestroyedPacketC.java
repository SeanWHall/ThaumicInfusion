package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 28/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class BlockDestroyedPacketC implements IMessage {

    public BlockDestroyedPacketC(){}

    private WorldCoord coordinates;

    public BlockDestroyedPacketC(WorldCoord coordinates){
        this.coordinates = coordinates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        coordinates = WorldCoord.get(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        coordinates.toBytes(buf);
    }

    public static class Handler implements IMessageHandler<BlockDestroyedPacketC, IMessage> {

        @Override
        public IMessage onMessage(BlockDestroyedPacketC message, MessageContext ctx) {
            WorldCoord coord = message.coordinates;
            if(coord == null || ctx.side.isServer()) return null;
            CooldownPacket.syncTimeouts.remove(coord);
            World world = ChannelHandler.getClientWorld();

            TIWorldData.getWorldData(world).removeBlock(coord);
            world.removeTileEntity(coord.x, coord.y, coord.z);
            return null;
        }
    }
}
