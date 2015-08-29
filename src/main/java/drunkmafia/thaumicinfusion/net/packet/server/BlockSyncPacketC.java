/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

public class BlockSyncPacketC implements IMessage {

    private BlockSavable data;
    private int chunkCount = 0;

    public BlockSyncPacketC() {
    }

    public BlockSyncPacketC(BlockSavable data, int chunkCount) {
        this.data = data;
        this.chunkCount = chunkCount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound tag = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
            if (tag != null) {
                data = SavableHelper.loadDataFromNBT(tag);
                chunkCount = buf.readInt();
            }
        } catch (Exception e) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (data != null) {
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(data));
                buf.writeInt(chunkCount);
            }
        } catch (Exception e) {}
    }

    public static class Handler implements IMessageHandler<BlockSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(BlockSyncPacketC message, MessageContext ctx) {
            BlockSavable data = message.data;
            if (data == null || ctx.side.isServer())
                return null;

            World world = ChannelHandler.getClientWorld();
            WorldCoordinates pos = data.getCoords();
            TIWorldData worldData = TIWorldData.getWorldData(world);

            worldData.removeData(message.data.getClass(), pos, false);
            worldData.addBlock(message.data, true, false);
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);

            if (message.chunkCount != -1 && worldData.chunkDatas.getCount() != message.chunkCount)
                ThaumicInfusion.getLogger().info("The Client has somehow become desynced with the server, please relog or reload this chunks!!");
            return null;
        }
    }
}
