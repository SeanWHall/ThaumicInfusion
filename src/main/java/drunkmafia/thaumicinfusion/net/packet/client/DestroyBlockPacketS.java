package drunkmafia.thaumicinfusion.net.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class DestroyBlockPacketS implements IMessage {

    public DestroyBlockPacketS() {}

    private WorldCoord coordinates;

    public DestroyBlockPacketS(WorldCoord coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if(buf.readByte() == 1){
            coordinates = new WorldCoord();
            coordinates.fromBytes(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(coordinates != null) {
            buf.writeByte(1);
            coordinates.dim = Minecraft.getMinecraft().theWorld.provider.dimensionId;
            coordinates.toBytes(buf);
        }else buf.writeByte(0);
    }

    public static class Handler implements IMessageHandler<DestroyBlockPacketS, IMessage> {
        @Override
        public IMessage onMessage(DestroyBlockPacketS message, MessageContext ctx) {
            if (message.coordinates == null || ctx.side.isClient()) return null;
            World world = ChannelHandler.getServerWorld(message.coordinates.dim);
            if(world != null)
                TIWorldData.getWorldData(world).removeBlock(message.coordinates, true);
            return null;
        }
    }
}
