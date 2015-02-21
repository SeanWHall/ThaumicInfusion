package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.IGoggles;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.common.items.armor.ItemGoggles;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("sensus"), cost = 4)
public class Sensus extends AspectEffect {

    boolean shouldRender, oldRender;

    @Override
    @SideOnly(Side.CLIENT)
    public void updateBlock(World world) {
        if(!world.isRemote)
            return;

        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.armorInventory[3];
        shouldRender = stack != null && stack.getItem() instanceof ItemGoggles;
        if(shouldRender != oldRender){
            oldRender = shouldRender;
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRender(World world, int x, int y, int z, RenderBlocks blocks) {
        ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.armorInventory[3];
        return stack != null && (stack.getItem() instanceof IRevealer || stack.getItem() instanceof IGoggles);
    }
}
