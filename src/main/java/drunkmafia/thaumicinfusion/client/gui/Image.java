package drunkmafia.thaumicinfusion.client.gui;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Image {
    public ResourceLocation image;
    public int x, y, u, v, width, height;

    private TIGui gui;

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
        gui.mc.renderEngine.bindTexture(image);
        GL11.glEnable(3042);
        gui.drawTexturedModalRect(gui.getGuiLeft() + x, gui.getGuiTop() + y, u, v, width, height);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public boolean isInRect(int mouseX, int mouseY) {
        return mouseX >= gui.getGuiLeft()  + x && mouseX <= gui.getGuiLeft()  + x + width && mouseY >= gui.getGuiTop()  + y && mouseY <= gui.getGuiTop()  + y + height;
    }

    public Image copy(){
        return new Image(gui, image, x, y, u, v, width, height);
    }

    public TIGui getGui(){
        return gui;
    }
}
