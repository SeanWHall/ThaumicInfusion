/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.server;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.helper.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
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

public class EffectSyncPacketC implements IMessage {

    private boolean updateRendering;
    private AspectEffect effect;
    private NBTTagCompound tagCompound;

    public EffectSyncPacketC() {
    }

    public EffectSyncPacketC(AspectEffect effect, boolean updateRendering) {
        this.effect = effect;
        this.updateRendering = updateRendering;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            NBTTagCompound tag = new PacketBuffer(buf).readNBTTagCompoundFromBuffer();
            if (tag != null) {
                this.tagCompound = tag;
                this.effect = SavableHelper.loadDataFromNBT(tag);
                this.updateRendering = buf.readByte() == 1;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            if (this.effect != null) {
                new PacketBuffer(buf).writeNBTTagCompoundToBuffer(SavableHelper.saveDataToNBT(this.effect));
                buf.writeByte(this.updateRendering ? 1 : 0);
            }
        } catch (Exception e) {
        }
    }

    public static class Handler implements IMessageHandler<EffectSyncPacketC, IMessage> {
        @Override
        public IMessage onMessage(EffectSyncPacketC message, MessageContext ctx) {
            AspectEffect effect = message.effect;
            if (effect == null || ctx.side.isServer()) return null;
            World world = ChannelHandler.getClientWorld();
            WorldCoordinates pos = effect.getPos();
            BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, effect.getPos());
            if (data != null && data.getEffect(effect.getClass()) != null)
                data.getEffect(effect.getClass()).readNBT(message.tagCompound);

            if (message.updateRendering) Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.pos);
            return null;
        }
    }
}
