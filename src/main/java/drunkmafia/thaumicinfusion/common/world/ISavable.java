package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by DrunkMafia on 08/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public interface ISavable {
    public void writeNBT(NBTTagCompound tagCompound);
    public void readNBT(NBTTagCompound tagCompound);
}

