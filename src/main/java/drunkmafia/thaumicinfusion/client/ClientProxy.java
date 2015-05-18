package drunkmafia.thaumicinfusion.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.gui.HumanusGUI;
import drunkmafia.thaumicinfusion.client.renderer.GreedEntityItemRenderer;
import drunkmafia.thaumicinfusion.client.renderer.InfusedBlockFallingRenderer;
import drunkmafia.thaumicinfusion.client.renderer.item.EssentiaBlockRenderer;
import drunkmafia.thaumicinfusion.common.CommonProxy;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.Humanus;
import drunkmafia.thaumicinfusion.common.aspect.entity.GreedEntityItem;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
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
        RenderingRegistry.registerEntityRenderingHandler(GreedEntityItem.class, new GreedEntityItemRenderer());

        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) {
            BlockData data = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
            if (data != null) {
                Humanus humanus = data.getEffect(Humanus.class);
                if (humanus != null) {
                    EntityPlayer target = humanus.getTarget();
                    if (target != null)
                        return new HumanusGUI(player.inventory, target.inventory);
                }
            }
        }
        return null;
    }
}
