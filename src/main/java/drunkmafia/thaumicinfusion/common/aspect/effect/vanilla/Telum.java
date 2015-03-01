package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("telum"), cost = 4)
public class Telum extends AspectEffect {
    @Override
    public boolean shouldRender(World world, int x, int y, int z, RenderBlocks renderBlocks) {
        GL11.glPushMatrix();

        GL11.glTranslatef(x, y + 1, z);
        RenderManager.instance.itemRenderer.renderItem(null, new ItemStack(Blocks.diamond_block), 0);

        GL11.glPopMatrix();

        return true;
    }
}
