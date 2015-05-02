package drunkmafia.thaumicinfusion.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
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

        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());
    }
}
