package drunkmafia.thaumicinfusion.client.renderer.item;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class InfusedItemRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (item.stackTagCompound == null)
            return;
        NBTTagCompound tag = item.stackTagCompound.getCompoundTag("InfuseTag");

        if (tag == null || !tag.hasKey("infusedID"))
            return;

        ItemStack infused = new ItemStack(Block.getBlockById(tag.getInteger("infusedID")), 1, item.getItemDamage());
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;

        if(player == null || Block.getBlockFromItem(infused.getItem()) == null || RenderManager.instance == null || RenderManager.instance.itemRenderer == null)
            return;

        GL11.glPushMatrix();

        if (type == ItemRenderType.EQUIPPED)
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
            GL11.glTranslatef(0.45F, 0.50F, 0.50F);

        try {
            RenderManager.instance.itemRenderer.renderItem(player, infused, 0);
        }catch (Exception e){}
        GL11.glPopMatrix();
    }
}
