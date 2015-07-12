package drunkmafia.thaumicinfusion.net.packet.client;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.aspects.Aspect;

import java.util.List;

public class WandAspectPacketS implements IMessage {

    private int playerName, slot, dim;
    private Aspect aspect;

    public WandAspectPacketS() {
    }

    public WandAspectPacketS(EntityPlayer player, int slotNumber, Aspect aspect) {
        playerName = player.getCommandSenderName().hashCode();
        dim = player.dimension;
        slot = slotNumber;
        this.aspect = aspect;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerName = buf.readInt();
        slot = buf.readInt();
        int hash = buf.readInt();
        dim = buf.readInt();

        for (Aspect aspect : AspectHandler.getAspects()) {
            if (aspect.getTag().hashCode() == hash) {
                this.aspect = aspect;
                break;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(playerName);
        buf.writeInt(slot);
        buf.writeInt(aspect.getTag().hashCode());
        buf.writeInt(dim);
    }

    public static class Handler implements IMessageHandler<WandAspectPacketS, IMessage> {
        @Override
        public IMessage onMessage(WandAspectPacketS message, MessageContext ctx) {
            if (message.aspect == null || ctx.side.isClient())
                return null;

            World world = DimensionManager.getWorld(message.dim);
            for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
                if (player.getCommandSenderName().hashCode() == message.playerName) {
                    ItemStack stack = player.inventory.mainInventory[message.slot];
                    NBTTagCompound compound = stack.getTagCompound() != null ? stack.getTagCompound() : new NBTTagCompound();
                    compound.setString("InfusionAspect", message.aspect.getTag());
                    stack.setTagCompound(compound);
                    return null;
                }
            }

            return null;
        }
    }
}