package drunkmafia.thaumicinfusion.client.gui;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Image {
    private final TIGui gui;
    public ResourceLocation image;
    public int x, y, u, v, width, height;

    public Image(TIGui gui, ResourceLocation image, int x, int y, int u, int v, int width, int height) {
        this.gui = gui;
        this.image = image;

        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    public void drawImage() {
        GL11.glPushMatrix();
        this.gui.mc.renderEngine.bindTexture(this.image);
        GL11.glEnable(3042);
        this.gui.drawTexturedModalRect(this.gui.getGuiLeft() + this.x, this.gui.getGuiTop() + this.y, this.u, this.v, this.width, this.height);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public boolean isInRect(int mouseX, int mouseY) {
        return mouseX >= this.gui.getGuiLeft() + this.x && mouseX <= this.gui.getGuiLeft() + this.x + this.width && mouseY >= this.gui.getGuiTop() + this.y && mouseY <= this.gui.getGuiTop() + this.y + this.height;
    }

    public Image copy() {
        return new Image(this.gui, this.image, this.x, this.y, this.u, this.v, this.width, this.height);
    }

    public TIGui getGui() {
        return this.gui;
    }
}
