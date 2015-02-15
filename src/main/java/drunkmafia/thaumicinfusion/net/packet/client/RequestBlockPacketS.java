package drunkmafia.thaumicinfusion.net.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 28/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class RequestBlockPacketS extends CooldownPacket {

    public RequestBlockPacketS() {}

    private WorldCoord coordinates;
    private Class<? extends BlockSavable> type;

    public RequestBlockPacketS(Class<? extends BlockSavable> type, WorldCoord coordinates) {
        if(!canSend(coordinates))
            return;
        this.coordinates = coordinates;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if(buf.readByte() == 1){
            coordinates = new WorldCoord();
            coordinates.fromBytes(buf);
            try {
                byte[] typeBytes = new byte[buf.readInt()];
                buf.readBytes(typeBytes);
                type = (Class<? extends BlockSavable>) Class.forName(new String(typeBytes));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(coordinates != null) {
            buf.writeByte(1);
            coordinates.toBytes(buf);
            byte[] typeBytes = type.getCanonicalName().getBytes();
            buf.writeInt(typeBytes.length);
            buf.writeBytes(typeBytes);
        }else buf.writeByte(0);
    }

    public static class Handler implements IMessageHandler<RequestBlockPacketS, IMessage> {
        @Override
        public IMessage onMessage(RequestBlockPacketS message, MessageContext ctx) {
            if (message.coordinates == null || ctx.side.isClient()) return null;
            WorldCoord pos = message.coordinates;
            World world = ChannelHandler.getServerWorld(pos.dim);

            BlockSavable data = TIWorldData.getData(message.type, world, pos);
            if (data != null)
                return new BlockSyncPacketC(data);
            else
                world.setBlock(pos.x, pos.y, pos.z, Blocks.air);
            return null;
        }
    }
}
