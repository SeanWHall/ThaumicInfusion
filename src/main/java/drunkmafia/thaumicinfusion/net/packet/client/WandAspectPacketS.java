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
        this.playerName = player.getCommandSenderName().hashCode();
        this.dim = player.dimension;
        this.slot = slotNumber;
        this.shouldOpenGUI = shouldOpenGUI;
        this.aspect = aspect;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerName = buf.readInt();
        this.slot = buf.readInt();
        if (buf.readInt() == 1) {
            int hash = buf.readInt();
            for (Aspect aspect : AspectHandler.getRegisteredAspects()) {
                if (aspect.getTag().hashCode() == hash) {
                    this.aspect = aspect;
                    break;
                }
            }
        }

        this.dim = buf.readInt();
        this.shouldOpenGUI = buf.readByte() == 1;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerName);
        buf.writeInt(this.slot);

        buf.writeInt(this.aspect != null ? 1 : -1);
        if (this.aspect != null) buf.writeInt(this.aspect != null ? this.aspect.getTag().hashCode() : -1);

        buf.writeInt(this.dim);
        buf.writeByte(this.shouldOpenGUI ? 1 : 0);
    }

    public static class Handler implements IMessageHandler<WandAspectPacketS, IMessage> {
        @Override
        public IMessage onMessage(WandAspectPacketS message, MessageContext ctx) {
            if (ctx.side.isClient())
                return null;

            World world = DimensionManager.getWorld(message.dim);
            for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
                if (player.getCommandSenderName().hashCode() == message.playerName) {
                    ItemStack stack = player.inventory.mainInventory[message.slot];
                    NBTTagCompound compound = stack.getTagCompound() != null ? stack.getTagCompound() : new NBTTagCompound();

                    if (message.aspect != null) compound.setString("InfusionAspect", message.aspect.getTag());
                    else if (compound.hasKey("InfusionAspect")) compound.removeTag("InfusionAspect");

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