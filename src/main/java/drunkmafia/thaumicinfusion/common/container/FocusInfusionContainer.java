package drunkmafia.thaumicinfusion.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Created by Sean on 04/04/2015.
 */
public class FocusInfusionContainer extends Container {

    public FocusInfusionContainer(EntityPlayer player){
        InventoryPlayer inv = player.inventory;
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(inv, x, 8 + 18 * x, 105));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + 18 * x, 47 + y * 18));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer p_75145_1_) {
        return true;
    }
}
