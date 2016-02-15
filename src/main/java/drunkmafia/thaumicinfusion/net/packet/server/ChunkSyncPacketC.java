/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import drunkmafia.thaumicinfusion.common.util.helper.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.internal.WorldCoordinates;

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
                this.data = SavableHelper.loadDataFromNBT(tag);
        } catch (Exception e) {
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (this.data != null)
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(this.data));
        } catch (Exception e) {
        }
    }

    public static class Handler implements IMessageHandler<ChunkSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(ChunkSyncPacketC message, MessageContext ctx) {
            ChunkData data = message.data;
            if (data == null || ctx.side.isServer())
                return null;
            World world = ChannelHandler.getClientWorld();
            TIWorldData worldData = TIWorldData.getWorldData(world);
            ChunkCoordIntPair chunkPos = data.getChunkPos();

            for (BlockSavable block : data.getAllBlocks())
                block.dataLoad(world);

            worldData.chunkDatas.set(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), data);

            for (BlockSavable savable : data.getAllBlocks()) {
                WorldCoordinates blockPos = savable.getCoords();
                world.markBlockForUpdate(blockPos.pos);
            }
            return null;
        }
    }
}
