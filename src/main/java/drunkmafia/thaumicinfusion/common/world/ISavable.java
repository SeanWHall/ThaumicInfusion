/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.nbt.NBTTagCompound;

public interface ISavable {
    void writeNBT(NBTTagCompound tagCompound);

    void readNBT(NBTTagCompound tagCompound);
}

