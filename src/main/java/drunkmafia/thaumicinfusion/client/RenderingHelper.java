/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.codechicken.lib.vec.Rotation;
import thaumcraft.codechicken.lib.vec.Scale;
import thaumcraft.codechicken.lib.vec.Translation;
import thaumcraft.common.blocks.BlockJar;
import thaumcraft.common.config.ConfigBlocks;

public final class RenderingHelper {

    private static final TextureManager textureManager = Minecraft.getMinecraft().renderEngine;

    public static void renderLiquid(Aspect aspect, double x, double y, double z) {
        if (aspect == null)
            return;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        RenderBlocks renderBlocks = new RenderBlocks();
        GL11.glDisable(2896);
        Tessellator t = Tessellator.instance;
        renderBlocks.setRenderBounds(0.365D, 0.0625D, 0.365D, 0.63D, 0.0625D, 0.63D);
        t.startDrawingQuads();
        t.setColorOpaque_I(aspect.getColor());

        int bright = 200;
        t.setBrightness(bright);
        IIcon icon = ((BlockJar) ConfigBlocks.blockJar).iconLiquid;
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        renderBlocks.renderFaceYNeg(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        renderBlocks.renderFaceYPos(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        renderBlocks.renderFaceZNeg(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        renderBlocks.renderFaceZPos(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        renderBlocks.renderFaceXNeg(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        renderBlocks.renderFaceXPos(ConfigBlocks.blockJar, -0.5D, 0.0D, -0.5D, icon);
        t.draw();
        GL11.glEnable(2896);
        GL11.glPopMatrix();
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
    }

    //Simple Renderer for basic models
    public static void renderSimpleModel(IModelCustom model, ResourceLocation texture, Translation translation, Scale scale, Rotation... rotations) {
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();

        translation.glApply();
        for (Rotation rot : rotations)
            GL11.glRotated(rot.angle, rot.axis.x, rot.axis.y, rot.axis.z);
        scale.glApply();

        textureManager.bindTexture(texture);
        model.renderAll();

        GL11.glPopMatrix();
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
    }

    public static void renderSimpleModel(IModelCustom model, String part, ResourceLocation texture, Translation translation, Scale scale, Rotation... rotations) {
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();

        translation.glApply();
        for (Rotation rot : rotations)
            GL11.glRotated(rot.angle, rot.axis.x, rot.axis.y, rot.axis.z);
        scale.glApply();

        textureManager.bindTexture(texture);
        model.renderPart(part);

        GL11.glPopMatrix();
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
    }


    public static void renderInventory(ItemStack item, Translation translation, float scaleModifer) {
        if (item == null) return;
        GL11.glPushMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);

        translation.glApply();
        if (item.getItem() instanceof ItemBlock)
            GL11.glScalef(1.5F * scaleModifer, 1.5F * scaleModifer, 1.5F * scaleModifer);
        else
            GL11.glScalef(1.0F * scaleModifer, 1.0F * scaleModifer, 1.0F * scaleModifer);

        item.stackSize = 1;
        EntityItem entityitem = new EntityItem(Minecraft.getMinecraft().theWorld, 0.0D, 0.0D, 0.0D, item);
        entityitem.hoverStart = 0.0F;

        RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);

        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }
}
