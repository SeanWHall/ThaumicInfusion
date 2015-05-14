package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class PlaySoundPacketC implements IMessage {

    double x, y, z;
    String sound;
    float vol, pitch;

    public PlaySoundPacketC() {
    }

    public PlaySoundPacketC(double x, double y, double z, String sound, float vol, float pitch){
        this.x = x;
        this.y = y;
        this.z = z;
        this.sound = sound;
        this.vol = vol;
        this.pitch = pitch;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();

        byte[] strBytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(strBytes);
        sound = new String(strBytes);
        vol = byteBuf.readFloat();
        pitch = byteBuf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);

        byte[] strBytes = sound.getBytes();
        byteBuf.writeInt(strBytes.length);
        byteBuf.writeBytes(strBytes);
        byteBuf.writeFloat(vol);
        byteBuf.writeFloat(pitch);
    }

    public static class Handler implements IMessageHandler<PlaySoundPacketC, IMessage> {

        @Override
        public IMessage onMessage(PlaySoundPacketC message, MessageContext ctx) {
            if(ctx.side.isServer() || message.sound == null)
                return null;
            Minecraft.getMinecraft().thePlayer.playSound(message.sound, message.vol, message.pitch);
            return null;
        }
    }
}
