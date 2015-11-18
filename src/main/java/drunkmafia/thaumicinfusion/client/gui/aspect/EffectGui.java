package drunkmafia.thaumicinfusion.client.gui.aspect;

import drunkmafia.thaumicinfusion.client.gui.TIGui;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import thaumcraft.api.internal.WorldCoordinates;

public class EffectGui extends TIGui {

    protected AspectEffect effect;
    protected int x, y, z;

    public EffectGui(AspectEffect effect) {
        this.effect = effect;
        WorldCoordinates pos = effect.getPos();
        x = pos.pos.getX();
        y = pos.pos.getY();
        z = pos.pos.getZ();
    }
}
