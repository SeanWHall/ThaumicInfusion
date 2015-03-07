package drunkmafia.thaumicinfusion.client.gui;

import drunkmafia.thaumicinfusion.common.container.FocusInfusionContainer;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class FocusInfusionGUI extends GuiContainer {
    private ResourceLocation gui = new ResourceLocation(ModInfo.MODID, "textures/gui/focusGUI.png");

    public FocusInfusionGUI(EntityPlayer player) {
        super(new FocusInfusionContainer(player));
        xSize = 176;
        ySize = 89;

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float mouseX, int mouseY, int tpf) {
        Minecraft.getMinecraft().renderEngine.bindTexture(gui);
        drawTexturedModalRect(guiLeft, guiTop + 40, 0, 0, xSize, ySize);

        drawTexturedModalRect(guiLeft + (xSize / 2) - 12, guiTop, 176, 0, 24, 24);
    }


}
