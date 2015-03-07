package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 29/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class BlockSavable implements ISavable {

    public boolean init = false;
    protected WorldCoord coordinates;

    public BlockSavable() {
    }

    public BlockSavable(WorldCoord coordinates) {
        this.coordinates = coordinates;
    }

    public void dataLoad(World world){
        init = true;
    }

    public boolean isInit(){
        return init;
    }

    public WorldCoord getCoords() {
        return coordinates;
    }

    public void setCoords(WorldCoord newPos){
        coordinates = newPos;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        coordinates.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        coordinates = new WorldCoord();
        coordinates.readNBT(tagCompound);
    }
}
