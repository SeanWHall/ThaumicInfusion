/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.renderer.InfusedBlockFallingRenderer;
import drunkmafia.thaumicinfusion.client.renderer.item.EssentiaBlockRenderer;
import drunkmafia.thaumicinfusion.common.CommonProxy;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public IModelCustom core, inscrber;

    public static ClientProxy getInstance() {
        return (ClientProxy) ThaumicInfusion.proxy;
    }

    @Override
    public void initRenderers() {
        //core = AdvancedModelLoader.loadModel(BlockInfo.infusionCoreBlock_Model);
        //inscrber = AdvancedModelLoader.loadModel(BlockInfo.inscriber_Model);

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TIBlocks.essentiaBlock), new EssentiaBlockRenderer());
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TIBlocks.aspectInscriber), new InscriberItemRenderer());

        //ClientRegistry.bindTileEntitySpecialRenderer(InscriberTile.class, new InscriberTileRenderer());

        RenderingRegistry.registerEntityRenderingHandler(InfusedBlockFalling.class, new InfusedBlockFallingRenderer());

        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());
    }
}
