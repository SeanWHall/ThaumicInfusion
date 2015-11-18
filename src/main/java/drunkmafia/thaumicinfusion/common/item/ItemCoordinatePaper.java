package drunkmafia.thaumicinfusion.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemCoordinatePaper extends Item {

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean val) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) return;
        if (Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)) {
            list.add("X: " + nbt.getInteger("CoordinateX"));
            list.add("Y: " + nbt.getInteger("CoordinateY"));
            list.add("Z: " + nbt.getInteger("CoordinateZ"));
            list.add("Dimension: " + nbt.getInteger("CoordinateDim"));
        } else list.add("Hold shift for more info");
    }
}
