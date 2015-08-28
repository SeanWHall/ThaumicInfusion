/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.ChunkSyncPacketC;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.ChunkCoordIntPair;

public class ChunkRequestPacketS implements IMessage {

    private ChunkCoordIntPair pos;
    private int dim;

    public ChunkRequestPacketS() {
    }

    public ChunkRequestPacketS(ChunkCoordIntPair pos, int dim) {
        this.pos = pos;
        this.dim = dim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
        dim = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getCenterXPos());
        buf.writeInt(pos.getCenterZPosition());

        buf.writeInt(dim);
    }

    public static class Handler implements IMessageHandler<ChunkRequestPacketS, IMessage> {
        @Override
        public IMessage onMessage(ChunkRequestPacketS message, MessageContext ctx) {
            ChunkCoordIntPair pos = message.pos;
            if (pos == null || ctx.side.isClient())
                return null;
            TIWorldData worldData = TIWorldData.getWorldData(ChannelHandler.getServerWorld(message.dim));
            if (worldData == null) return null;

            ChunkData data = worldData.chunkDatas.get(pos.getCenterXPos(), pos.getCenterZPosition(), null);
            if (data == null)
                return new ChunkSyncPacketC(data);
            return null;
        }
    }
}
