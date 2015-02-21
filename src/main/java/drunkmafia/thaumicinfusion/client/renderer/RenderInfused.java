package drunkmafia.thaumicinfusion.client.renderer;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

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
            Block dataBlock = data.getContainingBlock();
            if(dataBlock.getRenderType() == 0 && block.colorMultiplier(access, x, y, z) != 16777215) {
                RGB rgb = new RGB(block.colorMultiplier(access, x, y, z));
                return renderBlocks.renderStandardBlockWithColorMultiplier(dataBlock, x, y, z, rgb.getR(), rgb.getG(), rgb.getB());
            }else {
                return renderBlocks.renderBlockByRenderType(dataBlock, x, y, z);
            }
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
