/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.client;

import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChunkRequestPacketS implements IMessage {

    private ChunkCoordIntPair pos;
    private int dim;
    private String playerName;

    public ChunkRequestPacketS() {
    }

    public ChunkRequestPacketS(ChunkCoordIntPair pos, int dim, String playerName) {
        this.pos = pos;
        this.dim = dim;
        this.playerName = playerName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new ChunkCoordIntPair(buf.readInt(), buf.readInt());

        this.dim = buf.readInt();
        byte[] bytes = new byte[buf.readInt()];
        for (int i = 0; i < bytes.length; i++) bytes[i] = buf.readByte();
        this.playerName = new String(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pos.getCenterXPos() >> 4);
        buf.writeInt(this.pos.getCenterZPosition() >> 4);

        buf.writeInt(this.dim);
        buf.writeInt(playerName.getBytes().length);
        for (byte b : playerName.getBytes()) buf.writeByte(b);
    }

    public static class Handler implements IMessageHandler<ChunkRequestPacketS, IMessage> {
        @Override
        public IMessage onMessage(ChunkRequestPacketS message, MessageContext ctx) {
            ChunkCoordIntPair pos = message.pos;
            if (pos == null || ctx.side.isClient())
                return null;

            World world = ChannelHandler.getServerWorld(message.dim);

            TIWorldData worldData = TIWorldData.getWorldData(world);
            if (worldData == null)
                return null;

            EntityPlayer player = world.getPlayerEntityByName(message.playerName);

            ChunkData data = worldData.chunkDatas.get(pos.getCenterXPos(), pos.getCenterZPosition(), null);
            if (data != null) {
                for (BlockSavable savable : data.getAllBlocks())
                    if (savable != null)
                        ChannelHandler.instance().sendTo(new BlockSyncPacketC(savable), (EntityPlayerMP) player);
            }

            return null;
        }
    }
}
