package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "fabrico", cost = 4)
public class Fabrico extends AspectEffect {

    public Aspect aspect;

    @Override
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
        return aspect != null ? aspect.getColor() : 16777215;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack phial = player.getCurrentEquippedItem();
        if(world.isRemote) {
            if(phial != null && phial.getItem() instanceof IEssentiaContainerItem && ((IEssentiaContainerItem)phial.getItem()).getAspects(phial).getAspects()[0] != aspect){
                world.playSound((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);

                RGB rgb = new RGB(((IEssentiaContainerItem) phial.getItem()).getAspects(phial).getAspects()[0].getColor());

                for(int i = 0; i < 5; i++)
                    Thaumcraft.proxy.crucibleBubble(world, x + hitX, y + hitY, z + hitZ, rgb.getR(), rgb.getG(), rgb.getB());
                return true;
            }
            return false;
        }

        if(phial != null && phial.getItem() instanceof IEssentiaContainerItem){
            aspect = ((IEssentiaContainerItem)phial.getItem()).getAspects(phial).getAspects()[0];
            ChannelHandler.network.sendToAll(new EffectSyncPacketC(this));
            return true;
        }
        return false;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if(tagCompound.hasKey("aspect"))
            aspect = Aspect.getAspect(tagCompound.getString("aspect"));
        else
            aspect = null;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if(aspect != null)
            tagCompound.setString("aspect", aspect.getTag());
    }
}
