/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.renderer.item;

import drunkmafia.thaumicinfusion.client.ClientProxy;
import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import thaumcraft.codechicken.lib.vec.Rotation;
import thaumcraft.codechicken.lib.vec.Scale;
import thaumcraft.codechicken.lib.vec.Translation;

import static drunkmafia.thaumicinfusion.client.RenderingHelper.renderSimpleModel;

public class InscriberItemRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
            GL11.glTranslatef(0, 0.4F, 0);

        ClientProxy proxy = ClientProxy.getInstance();

        //Renders Base
        renderSimpleModel(proxy.inscrber, "Base_Cube", BlockInfo.inscriber_Texture, new Translation(0.5D, 0, 0.5D), new Scale(1, 1, 1));

        //Renders Lid
        renderSimpleModel(proxy.inscrber, "Lid_Cube.002", BlockInfo.inscriber_Texture, new Translation(0, 0.25D, 0.5D), new Scale(1, 1, 1));
        renderSimpleModel(proxy.inscrber, "Lid_Cube.002", BlockInfo.inscriber_Texture, new Translation(0 + 1D, 0.25D, 0.5D), new Scale(1, 1, 1), new Rotation(180, 0, 1, 0));
    }
}
