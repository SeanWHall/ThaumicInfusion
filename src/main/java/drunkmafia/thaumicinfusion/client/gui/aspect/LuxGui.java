package drunkmafia.thaumicinfusion.client.gui.aspect;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;

public class LuxGui extends EffectGui {

    public LuxGui(AspectEffect effect) {
        super(effect);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tpf) {
        drawDefaultBackground();
    }
}
