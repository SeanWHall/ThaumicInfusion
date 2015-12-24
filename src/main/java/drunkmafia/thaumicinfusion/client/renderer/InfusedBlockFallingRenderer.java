/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.renderer;

import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class InfusedBlockFallingRenderer extends Render {

    public InfusedBlockFallingRenderer() {
        super(Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_) {
        try {
            InfusedBlockFalling movingEntity = (InfusedBlockFalling) entity;

            GL11.glPushMatrix();

            GL11.glTranslated(x, y, z);
            GL11.glRotatef(-90, 0, 1F, 0);
            GL11.glScalef(4F, 4F, 4F);

            EntityItem entityitem = new EntityItem(entity.worldObj, 0.0D, 0.0D, 0.0D, new ItemStack(Block.getBlockById(movingEntity.id), 1, movingEntity.meta));
            entityitem.hoverStart = 0F;

            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            if (!Minecraft.isFancyGraphicsEnabled()) {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
            }
            GL11.glPopMatrix();
        } catch (Throwable t) {/** Catches any exceptions thrown when attempting to render a block **/}
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }
}
