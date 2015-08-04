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
    private boolean shouldOpenGUI;

    public WandAspectPacketS() {
    }

    public WandAspectPacketS(EntityPlayer player, int slotNumber, Aspect aspect, boolean shouldOpenGUI) {
        playerName = player.getCommandSenderName().hashCode();
        dim = player.dimension;
        slot = slotNumber;
        this.shouldOpenGUI = shouldOpenGUI;
        this.aspect = aspect;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerName = buf.readInt();
        slot = buf.readInt();
        int hash = buf.readInt();
        dim = buf.readInt();

        for (Aspect aspect : AspectHandler.getRegisteredAspects()) {
            if (aspect.getTag().hashCode() == hash) {
                this.aspect = aspect;
                break;
            }
        }
        shouldOpenGUI = buf.readByte() == 1;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(playerName);
        buf.writeInt(slot);
        buf.writeInt(aspect.getTag().hashCode());
        buf.writeInt(dim);
        buf.writeByte(shouldOpenGUI ? 1 : 0);
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
                    compound.setBoolean("isSelected", message.shouldOpenGUI);
                    stack.setTagCompound(compound);
                    player.inventory.mainInventory[message.slot] = stack;
                    return null;
                }
            }
            return null;
        }
    }
}