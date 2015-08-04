package drunkmafia.thaumicinfusion.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public abstract class TIGui extends GuiScreen implements IGUI {

    protected int guiLeft, guiTop, xSize, ySize;

    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
    }

    @Override
    public void drawHoveringText(List list, int x, int y, FontRenderer font) {
        super.drawHoveringText(list, x, y, font);
    }

    public FontRenderer getFontRenderer(){
        return fontRendererObj;
    }

    public int getGuiLeft(){
        return guiLeft;
    }

    public int getGuiTop(){
        return guiTop;
    }

    public int getXSize(){
        return xSize;
    }

    public int getYSize(){
        return ySize;
    }
}
