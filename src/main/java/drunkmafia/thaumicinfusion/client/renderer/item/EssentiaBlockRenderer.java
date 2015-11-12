/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.renderer.item;

import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.util.RGB;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;

public class EssentiaBlockRenderer implements IItemRenderer {
    @Override
    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack stack, Object... data) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return;


        Block essentiaBlock = TIBlocks.essentiaBlock;
        RenderBlocks renderBlocks = new RenderBlocks();
        Tessellator tessellator = Tessellator.instance;

        essentiaBlock.setBlockBoundsForItemRender();
        renderBlocks.setRenderBoundsFromBlock(essentiaBlock);

        GL11.glPushMatrix();

        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);

        if (type == IItemRenderer.ItemRenderType.EQUIPPED)
            GL11.glTranslatef(-1.1225F, 0.05F, 0F);
        else if (type == IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
            GL11.glTranslatef(-1.F, 0F, 0.05F);
        else
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        new RGB(Aspect.getAspect(tag.getString("aspectTag")).getColor()).glColor3f();

        IIcon icon = renderBlocks.getBlockIconFromSideAndMetadata(essentiaBlock, 0, stack.getItemDamage());

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderBlocks.renderFaceYNeg(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderBlocks.renderFaceYPos(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderBlocks.renderFaceZNeg(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderBlocks.renderFaceZPos(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderBlocks.renderFaceXNeg(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderBlocks.renderFaceXPos(essentiaBlock, 0.0D, 0.0D, 0.0D, icon);
        tessellator.draw();

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glPopMatrix();
    }
}
