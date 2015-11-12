package drunkmafia.thaumicinfusion.client.gui;

import org.lwjgl.opengl.GL11;

public class Button {

    private final Image normal;
    private final Image hover;
    private final Image selected;
    private final Image icon;
    private final String text;
    private final Runnable runnable;
    private final TIGui gui;
    public boolean isSelected;

    public Button(Image normal, Image hover, Image selected, Image icon, String text, Runnable runnable) {
        this.normal = normal;
        this.hover = hover;
        this.selected = selected;
        this.icon = icon;
        this.text = text;
        this.runnable = runnable;

        this.gui = normal.getGui();
    }


    public void drawButton(int mouseX, int mouseY) {
        if (this.hover.isInRect(mouseX, mouseY)) this.hover.drawImage();
        else if (this.isSelected) this.selected.drawImage();
        else this.normal.drawImage();

        if (this.icon != null) this.icon.drawImage();

        if (this.text != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.gui.getGuiLeft(), this.gui.getGuiTop(), 0.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            this.gui.getFontRenderer().drawString(this.text, this.normal.x, this.normal.y, 1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    public void onMouseClick(int mouseX, int mouseY) {
        if (this.normal.isInRect(mouseX, mouseY) || this.selected.isInRect(mouseX, mouseY) || this.hover.isInRect(mouseX, mouseY)) {
            this.isSelected = !this.isSelected;
            if (this.runnable != null) this.runnable.run();
        }
    }
}
