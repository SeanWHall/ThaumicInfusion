package drunkmafia.thaumicinfusion.net.packet.server;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class EntitySyncPacketC implements IMessage {

    public EntitySyncPacketC() {}

    private int id;
    private NBTTagCompound tagCompound;
    private Entity entity;

    public EntitySyncPacketC(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            id = buf.readInt();
            tagCompound = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (entity != null) {
                buf.writeInt(entity.getEntityId());
                NBTTagCompound tag = new NBTTagCompound();
                entity.writeToNBT(tag);
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

            if(entity != null)
                entity.readFromNBT(tag);

            return null;
        }
    }
}
