package drunkmafia.thaumicinfusion.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class InfusionContainer extends Container {
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
