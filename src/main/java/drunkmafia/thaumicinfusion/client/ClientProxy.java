/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client;

import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.gui.InfusionGui;
import drunkmafia.thaumicinfusion.client.renderer.InfusedBlockFallingRenderer;
import drunkmafia.thaumicinfusion.common.CommonProxy;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import thaumcraft.api.items.ItemsTC;

public class ClientProxy extends CommonProxy {

    @Override
    public void initRenderers() {
        CommonProxy.isClient = true;

        RenderingRegistry.registerEntityRenderingHandler(InfusedBlockFalling.class, new InfusedBlockFallingRenderer());
        MinecraftForge.EVENT_BUS.register(new ClientEventContainer());

        ItemModelMesher modelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        modelMesher.register(TIItems.focusInfusing, 0, new ModelResourceLocation(ModInfo.MODID + ":focus_infusion", "inventory"));
        modelMesher.register(TIItems.coordinatePaper, 0, new ModelResourceLocation(ModInfo.MODID + ":coordinate_paper", "inventory"));
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0 && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemsTC.wand.getClass()) && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) != null && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing)
            return new InfusionGui(player, player.getCurrentEquippedItem());
        return null;
    }
}
