/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class ChunkSyncPacketC implements IMessage {

    private ChunkData data;

    public ChunkSyncPacketC() {
    }

    public ChunkSyncPacketC(ChunkData data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound tag = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
            if (tag != null)
                data = SavableHelper.loadDataFromNBT(tag);
        } catch (Exception e) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (data != null)
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(data));
            new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(data));

        } catch (Exception e) {}
    }

    public static class Handler implements IMessageHandler<ChunkSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(ChunkSyncPacketC message, MessageContext ctx) {
            ChunkData data = message.data;
            if (data == null || ctx.side.isServer())
                return null;

            TIWorldData worldData = TIWorldData.getWorldData(ChannelHandler.getClientWorld());
            worldData.chunkDatas.set(data, data.getChunkPos().getCenterXPos(), data.getChunkPos().getCenterZPosition());
            return null;
        }
    }
}
