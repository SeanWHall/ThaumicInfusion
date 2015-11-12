package drunkmafia.thaumicinfusion.client.gui;

import java.util.Arrays;

public class RadioButton {

    private final Image background;
    private final Image checked;
    public String[] hoverText;
    public boolean isChecked;

    public RadioButton(Image background, Image checked, boolean isChecked, String... hoverText) {
        this.isChecked = isChecked;
        this.hoverText = hoverText;
        this.background = background;
        this.checked = checked;
    }

    public void onMouseClick(int mouseX, int mouseY) {
        if (this.background.isInRect(mouseX, mouseY)) this.isChecked = !this.isChecked;
    }

    public void drawImage(int mouseX, int mouseY) {
        this.background.drawImage();
        if (this.isChecked) this.checked.drawImage();
        if (this.background.isInRect(mouseX, mouseY))
            this.background.getGui().drawHoveringText(Arrays.asList(this.hoverText), this.background.getGui().getGuiLeft() + this.background.x, this.background.getGui().getGuiTop() + this.background.y, this.background.getGui().getFontRenderer());
    }
}
