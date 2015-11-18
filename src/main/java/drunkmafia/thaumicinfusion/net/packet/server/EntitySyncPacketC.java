/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EntitySyncPacketC implements IMessage {

    private int id;
    private NBTTagCompound tagCompound;
    private Entity entity;

    public EntitySyncPacketC() {
    }

    public EntitySyncPacketC(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            this.id = buf.readInt();
            this.tagCompound = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (this.entity != null) {
                buf.writeInt(this.entity.getEntityId());
                NBTTagCompound tag = new NBTTagCompound();
                this.entity.writeToNBT(tag);
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Handler implements IMessageHandler<EntitySyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(EntitySyncPacketC message, MessageContext ctx) {
            NBTTagCompound tag = message.tagCompound;
            if (tag == null || ctx.side.isServer()) return null;
            Entity entity = ChannelHandler.getClientWorld().getEntityByID(message.id);

            if (entity != null)
                entity.readFromNBT(tag);

            return null;
        }
    }
}
