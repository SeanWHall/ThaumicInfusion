package drunkmafia.thaumicinfusion.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.codechicken.lib.vec.Vector3;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.infusionCore_Model;
import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.infusionCore_Texture;

/**
 * Created by DrunkMafia on 20/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class CoreItemRenderer implements IItemRenderer {

    IModelCustom infusionCore = AdvancedModelLoader.loadModel(infusionCore_Model);

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
        renderCore(new Vector3(0.5F, 0.5F, 0.5F), new Vector3(1.4F, 1.3F, 1.4F), new Vector3(0, 0, 0), 0);
        renderCore(new Vector3(0.5F, 0.5F, 0.5F), new Vector3(1F, 1.3F, 1F), new Vector3(0, 0, 0), 0);
    }

    public void renderCore(Vector3 coords, Vector3 scale, Vector3 rotation, float angle) {
        GL11.glPushMatrix();

        GL11.glTranslated(coords.x, coords.y, coords.z);
        GL11.glScaled(scale.x, scale.y, scale.z);
        GL11.glRotated(angle, rotation.x, rotation.y, rotation.z);

        Minecraft.getMinecraft().renderEngine.bindTexture(infusionCore_Texture);
        infusionCore.renderAll();

        GL11.glPopMatrix();
    }
}
