package drunkmafia.thaumicinfusion.client.gui;

import net.minecraft.client.gui.GuiScreen;

/**
 * Created by DrunkMafia on 22/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public abstract class EffectGUI extends GuiScreen {

    public int guiLeft, guiTop, xSize, ySize;

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    protected abstract void drawGuiContainerBackgroundLayer(int mouseX, int mouseY, float tpf);
    protected abstract void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float tpf);
}
