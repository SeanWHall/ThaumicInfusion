package drunkmafia.thaumicinfusion.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public abstract class TIGui extends GuiScreen implements IGUI {

    protected int guiLeft, guiTop, xSize, ySize;

    public void initGui() {
        super.initGui();
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
    }

    @Override
    public void drawHoveringText(List list, int x, int y, FontRenderer font) {
        super.drawHoveringText(list, x, y, font);
    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    public int getGuiLeft() {
        return this.guiLeft;
    }

    public int getGuiTop() {
        return this.guiTop;
    }

    public int getXSize() {
        return this.xSize;
    }

    public int getYSize() {
        return this.ySize;
    }
}
