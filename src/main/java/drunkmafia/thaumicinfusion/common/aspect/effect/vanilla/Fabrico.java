/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;

@Effect(aspect = "fabrico")
public class Fabrico extends AspectEffect {

    public Aspect aspect;

    @Override
    public int getCost() {
        return 4;
    }

    @OverrideBlock
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
        return this.aspect != null ? this.aspect.getColor() : access.getBlock(x, y, z).getBlockColor();
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack phial = player.getCurrentEquippedItem();
        if (world.isRemote) {
            if (phial != null && phial.getItem() instanceof IEssentiaContainerItem) {
                AspectList aspects = ((IEssentiaContainerItem) phial.getItem()).getAspects(phial);
                if (aspects != null && aspects.getAspects()[0] != this.aspect) {
                    world.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);

                    RGB rgb = new RGB(((IEssentiaContainerItem) phial.getItem()).getAspects(phial).getAspects()[0].getColor());
                    for (int i = 0; i < 5; i++)
                        Thaumcraft.proxy.crucibleBubble(world, x, y, z, rgb.getR(), rgb.getG(), rgb.getB());
                }
            }
            return;
        }

        if (phial != null && phial.getItem() instanceof IEssentiaContainerItem) {
            AspectList aspects = ((IEssentiaContainerItem) phial.getItem()).getAspects(phial);
            this.aspect = aspects != null ? aspects.getAspects()[0] : null;
            ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
        }
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.hasKey("aspect"))
            this.aspect = Aspect.getAspect(tagCompound.getString("aspect"));
        else
            this.aspect = null;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if (this.aspect != null)
            tagCompound.setString("aspect", this.aspect.getTag());
    }
}
