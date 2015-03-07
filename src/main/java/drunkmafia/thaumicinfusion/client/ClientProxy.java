package drunkmafia.thaumicinfusion.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.event.ClientTickHandler;
import drunkmafia.thaumicinfusion.client.gui.FocusInfusionGUI;
import drunkmafia.thaumicinfusion.client.gui.InfusedBlockGUI;
import drunkmafia.thaumicinfusion.client.renderer.InfusedBlockFallingRenderer;
import drunkmafia.thaumicinfusion.client.renderer.item.EssentiaBlockRenderer;
import drunkmafia.thaumicinfusion.common.CommonProxy;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public static ClientProxy getInstance() {
        return (ClientProxy) ThaumicInfusion.proxy;
    }

    @Override
    public void initRenderers() {
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(TIBlocks.essentiaBlock), new EssentiaBlockRenderer());

        RenderingRegistry.registerEntityRenderingHandler(InfusedBlockFalling.class, new InfusedBlockFallingRenderer());

        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case 0:
                return new InfusedBlockGUI(new WorldCoord(x, y, z));
            case 1:
                return new FocusInfusionGUI(player);
            default:
                return null;
        }
    }
}
