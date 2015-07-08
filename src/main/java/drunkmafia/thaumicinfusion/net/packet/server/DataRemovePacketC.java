/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

public class DataRemovePacketC implements IMessage {

    private Class clazz;
    private WorldCoordinates coordinates;

    public DataRemovePacketC() {
    }

    public DataRemovePacketC(Class clazz, WorldCoordinates coordinates) {
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

            coordinates = new WorldCoordinates(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if(coordinates != null) {
            buf.writeByte(1);

            byte[] bytes = clazz.getName().getBytes();
            buf.writeInt(bytes.length);
            for (byte aByte : bytes) buf.writeByte(aByte);

            buf.writeInt(coordinates.x);
            buf.writeInt(coordinates.y);
            buf.writeInt(coordinates.z);
            buf.writeInt(coordinates.dim);
        }else buf.writeByte(0);
    }

    public static class Handler implements IMessageHandler<DataRemovePacketC, IMessage> {
        @Override
        public IMessage onMessage(DataRemovePacketC message, MessageContext ctx) {
            WorldCoordinates pos = message.coordinates;

            if (pos == null || ctx.side.isServer()) return null;
            World world = ChannelHandler.getClientWorld();

            if (world != null && world.provider.dimensionId == pos.dim)
                TIWorldData.getWorldData(world).removeData(message.clazz, pos, false);
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);
            return null;
        }
    }
}
