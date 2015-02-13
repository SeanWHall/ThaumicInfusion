package drunkmafia.thaumicinfusion.client.renderer;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.ClientProxy;
import thaumcraft.common.Thaumcraft;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@SideOnly(Side.CLIENT)
public class RenderInfused implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int i, int i1, RenderBlocks renderBlocks) {}

    @Override
    public boolean renderWorldBlock(IBlockAccess access, int x, int y, int z, Block block, int meta, RenderBlocks renderBlocks) {
        BlockData data = TIWorldData.getData(BlockData.class, Minecraft.getMinecraft().theWorld, new WorldCoord(x, y, z));
        if (data == null)
            return false;

        for (AspectEffect effects : data.getEffects())
            if (!effects.shouldRender(Minecraft.getMinecraft().theWorld, x, y, z, renderBlocks))
                return false;
        try {
            return renderBlocks.renderBlockByRenderType(data.getContainingBlock(), x, y, z);
        }catch (Exception e){}
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int i) {
        return false;
    }

    @Override
    public int getRenderId() {
        if(InfusedBlock.renderType == -1)
            InfusedBlock.renderType = RenderingRegistry.getNextAvailableRenderId();
        return InfusedBlock.renderType;
    }
}
