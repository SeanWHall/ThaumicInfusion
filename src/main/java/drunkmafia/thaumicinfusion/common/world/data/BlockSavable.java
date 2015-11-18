/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.common.world.ISavable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

public class BlockSavable implements ISavable {

    public boolean init;
    protected WorldCoordinates coordinates;

    public BlockSavable() {
    }

    public BlockSavable(WorldCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void dataLoad(World world) {
        this.init = true;
    }

    public boolean isInit() {
        return this.init;
    }

    public WorldCoordinates getCoords() {
        return this.coordinates;
    }

    public void setCoords(WorldCoordinates newPos) {
        this.coordinates = newPos;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        this.coordinates.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        this.coordinates = new WorldCoordinates();
        this.coordinates.readNBT(tagCompound);
    }
}
