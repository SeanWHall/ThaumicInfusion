/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.internal.WorldCoordinates;

public class BlockSyncPacketC implements IMessage {

    private BlockSavable data;

    public BlockSyncPacketC() {
    }

    public BlockSyncPacketC(BlockSavable data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound tag = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
            if (tag != null) {
                this.data = SavableHelper.loadDataFromNBT(tag);
            }
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

    public static class Handler implements IMessageHandler<BlockSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(BlockSyncPacketC message, MessageContext ctx) {
            BlockSavable data = message.data;
            if (data == null || ctx.side.isServer())
                return null;

            World world = ChannelHandler.getClientWorld();
            WorldCoordinates pos = data.getCoords();
            TIWorldData worldData = TIWorldData.getWorldData(world);

            if (worldData == null) return null;

            //Packet can arrive before the worlds rendering has full initialized
            try {
                worldData.removeData(data.getClass(), pos, false);
                worldData.addBlock(data, true, false);
                Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.pos);
            } catch (Exception e) {
            }

            return null;
        }
    }
}
