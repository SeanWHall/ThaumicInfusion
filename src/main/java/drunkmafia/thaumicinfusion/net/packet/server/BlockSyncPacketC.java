/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import drunkmafia.thaumicinfusion.client.world.ClientBlockData;
import drunkmafia.thaumicinfusion.common.util.helper.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.internal.WorldCoordinates;

public class BlockSyncPacketC implements IMessage {

    private BlockSavable data;
    private NBTTagCompound nbtTagCompound;

    public BlockSyncPacketC() {
    }

    public BlockSyncPacketC(BlockSavable data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            nbtTagCompound = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
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
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(BlockSyncPacketC message, MessageContext ctx) {
            ClientBlockData data = new ClientBlockData();

            if (message.nbtTagCompound == null || ctx.side.isServer())
                return null;

            data.readNBT(message.nbtTagCompound);

            World world = ChannelHandler.getClientWorld();
            WorldCoordinates pos = data.getCoords();
            TIWorldData worldData = TIWorldData.getWorldData(world);

            if (worldData == null) return null;

            //Packet can arrive before the worlds rendering has full initialized
            try {
                worldData.removeData(BlockData.class, pos, false);
                worldData.addBlock(data, true, false);
                Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.pos);
            } catch (Exception ignored) {
            }

            return null;
        }
    }
}
