/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.nbt.NBTTagCompound;

public class SavableHelper {
    public static <T>T loadDataFromNBT(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("class")) return null;
        try {
            Class<?> c = Class.forName(tag.getString("class"));
            if (ISavable.class.isAssignableFrom(c)) {
                ISavable data = (ISavable) c.newInstance();
                data.readNBT(tag);
                return (T) data;
            }
        } catch (Exception e) {}
        return null;
    }

    public static NBTTagCompound saveDataToNBT(ISavable savable){
        NBTTagCompound tag = new NBTTagCompound();
        if(savable == null) return tag;
        tag.setString("class", savable.getClass().getCanonicalName());
        savable.writeNBT(tag);
        return tag;
    }
}
