package drunkmafia.thaumicinfusion.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class InfusionContainer extends Container {

    public InfusionContainer(InventoryPlayer inventoryPlayer){

        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 14 + x * 18, 145 + y * 18));
            }
        }

        for(int x = 0; x < 9; ++x) {
            this.addSlotToContainer(new Slot(inventoryPlayer, x, 14 + x * 18, 203));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
