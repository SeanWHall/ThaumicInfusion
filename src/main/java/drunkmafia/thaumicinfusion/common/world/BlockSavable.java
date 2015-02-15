package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 29/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public abstract class BlockSavable implements ISavable {

    protected WorldCoord coordinates;
    protected int blockID;

    public BlockSavable() {
    }

    public BlockSavable(WorldCoord coordinates, int blockID) {
        this.coordinates = coordinates;
        this.blockID = blockID;
    }

    public boolean init = false;

    public void dataLoad(World world){
        init = true;
    }

    public boolean isInit(){
        return init;
    }

    @Override
    public abstract boolean equals(Object obj);

    public WorldCoord getCoords() {
        return coordinates;
    }

    public void setCoords(WorldCoord newPos){
        coordinates = newPos;
    }

    public Block getBlock(){
        return Block.getBlockById(blockID);
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("blockID", blockID);
        coordinates.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        blockID = tagCompound.getInteger("blockID");
        coordinates = new WorldCoord();
        coordinates.readNBT(tagCompound);
    }
}
