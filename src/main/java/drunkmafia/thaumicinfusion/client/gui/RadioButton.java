package drunkmafia.thaumicinfusion.client.gui;

import java.util.Arrays;

public class RadioButton {

    private Image background, checked;

    public String[] hoverText;
    public boolean isChecked;

    public RadioButton(Image background, Image checked, boolean isChecked, String... hoverText){
        this.isChecked = isChecked;
        this.hoverText = hoverText;
        this.background = background;
        this.checked = checked;
    }

    public void onMouseClick(int mouseX, int mouseY){
        if(background.isInRect(mouseX, mouseY)) isChecked = !isChecked;
    }

    public void drawImage(int mouseX, int mouseY){
        background.drawImage();
        if(isChecked) checked.drawImage();
        if(background.isInRect(mouseX, mouseY)) background.getGui().drawHoveringText(Arrays.asList(hoverText), background.getGui().getGuiLeft() + background.x,  background.getGui().getGuiTop() + background.y,  background.getGui().getFontRenderer());
    }
}
