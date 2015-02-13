package drunkmafia.thaumicinfusion.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.event.ClientTickHandler;
import drunkmafia.thaumicinfusion.client.gui.InfusedBlockGUI;
import drunkmafia.thaumicinfusion.client.renderer.InfusedBlockFallingRenderer;
import drunkmafia.thaumicinfusion.client.renderer.RenderInfused;
import drunkmafia.thaumicinfusion.client.renderer.item.CoreItemRenderer;
import drunkmafia.thaumicinfusion.client.renderer.item.EssentiaBlockRenderer;
import drunkmafia.thaumicinfusion.client.renderer.item.InfusedItemRenderer;
import drunkmafia.thaumicinfusion.client.renderer.tile.InfusionCoreRenderer;
import drunkmafia.thaumicinfusion.common.CommonProxy;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.block.tile.InfusionCoreTile;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void initRenderers() {
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TIBlocks.essentiaBlock), new EssentiaBlockRenderer());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TIBlocks.infusionCoreBlock), new CoreItemRenderer());

        InfusedBlock[] blocks = BlockHandler.getBlocks();
        for (InfusedBlock block : blocks)
            MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), new InfusedItemRenderer());

        RenderingRegistry.registerBlockHandler(new RenderInfused());
        RenderingRegistry.registerEntityRenderingHandler(InfusedBlockFalling.class, new InfusedBlockFallingRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(InfusionCoreTile.class, new InfusionCoreRenderer());

        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0:
                return new InfusedBlockGUI(new WorldCoord(x, y, z));
            default:
                return null;
        }
    }

    public static ClientProxy getInstance(){
        return (ClientProxy) ThaumicInfusion.proxy;
    }
}
