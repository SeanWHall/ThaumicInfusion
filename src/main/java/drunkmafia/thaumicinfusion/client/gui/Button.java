package drunkmafia.thaumicinfusion.client.gui;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import org.lwjgl.opengl.GL11;

public class Button {

    private Image normal, hover, selected, icon;
    private String text;
    private Runnable runnable;

    public boolean isSelected;

    private TIGui gui;

    public Button(Image normal, Image hover, Image selected, Image icon, String text, Runnable runnable){
        this.normal = normal;
        this.hover = hover;
        this.selected = selected;
        this.icon = icon;
        this.text = text;
        this.runnable = runnable;

        gui = normal.getGui();
    }


    public void drawButton(int mouseX, int mouseY){
        if(hover.isInRect(mouseX, mouseY)) hover.drawImage();
        else if(isSelected) selected.drawImage();
        else normal.drawImage();

        if(icon != null) icon.drawImage();

        if(text != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(gui.getGuiLeft(), gui.getGuiTop(), 0.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            gui.getFontRenderer().drawString(text, normal.x, normal.y, 1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    public void onMouseClick(int mouseX, int mouseY){
        if(normal.isInRect(mouseX, mouseY) || selected.isInRect(mouseX, mouseY) || hover.isInRect(mouseX, mouseY)){
            isSelected = !isSelected;
            if(runnable != null) runnable.run();
        }
    }
}
