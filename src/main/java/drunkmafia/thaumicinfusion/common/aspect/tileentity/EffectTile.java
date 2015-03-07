package drunkmafia.thaumicinfusion.common.aspect.tileentity;

import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by DrunkMafia on 10/10/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class EffectTile extends TileEntity {

    @Override
    public void invalidate() {
        super.invalidate();
        BlockData data = TIWorldData.getWorldData(worldObj).getBlock(BlockData.class, new WorldCoord(xCoord, yCoord, zCoord));
        if (data != null) {
            NBTTagCompound tileTag = new NBTTagCompound();
            writeToNBT(tileTag);
            data.tileTag = tileTag;
        }
    }
}
