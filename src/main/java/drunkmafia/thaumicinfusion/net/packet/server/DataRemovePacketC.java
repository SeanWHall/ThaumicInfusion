package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

/**
 * Created by Sean on 09/04/2015.
 */
public class DataRemovePacketC implements IMessage {

    private Class clazz;
    private WorldCoord coordinates;

    public DataRemovePacketC() {
    }

    public DataRemovePacketC(Class clazz, WorldCoord coordinates) {
        this.clazz = clazz;
        this.coordinates = coordinates;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if(buf.readByte() == 1){
            byte[] bytes = new byte[buf.readInt()];
            for(int i = 0; i < bytes.length; i++)
                bytes[i] = buf.readByte();

            try{
                clazz =  Class.forName(new String(bytes));
            }catch (Exception e){
                e.printStackTrace();
            }

            coordinates = new WorldCoord();
            coordinates.fromBytes(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(coordinates != null) {
            buf.writeByte(1);

            byte[] bytes = clazz.getName().getBytes();
            buf.writeInt(bytes.length);
            for (byte aByte : bytes) buf.writeByte(aByte);

            coordinates.toBytes(buf);
        }else buf.writeByte(0);
    }

    public static class Handler implements IMessageHandler<DataRemovePacketC, IMessage> {
        @Override
        public IMessage onMessage(DataRemovePacketC message, MessageContext ctx) {
            if (message.coordinates == null || ctx.side.isServer()) return null;
            World world = ChannelHandler.getClientWorld();

            if(world != null && world.provider.dimensionId == message.coordinates.dim)
                TIWorldData.getWorldData(world).removeData(message.clazz, message.coordinates, false);

            return null;
        }
    }
}
