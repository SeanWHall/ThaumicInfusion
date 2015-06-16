/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.common.world.ISavable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

public class BlockSavable implements ISavable {

    public boolean init = false;
    protected WorldCoordinates coordinates;

    public BlockSavable() {
    }

    public BlockSavable(WorldCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void dataLoad(World world){
        init = true;
    }

    public boolean isInit(){
        return init;
    }

    public WorldCoordinates getCoords() {
        return coordinates;
    }

    public void setCoords(WorldCoordinates newPos) {
        coordinates = newPos;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        coordinates.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        coordinates = new WorldCoordinates();
        coordinates.readNBT(tagCompound);
    }
}
