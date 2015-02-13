package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.aspects.Aspect;

/**
 * Created by DrunkMafia on 29/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class EssentiaData extends BlockSavable {

    private Aspect aspect;

    public EssentiaData(){}

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EssentiaData && ((EssentiaData)obj).aspect == this.aspect;
    }

    public EssentiaData(WorldCoord coordinates, Aspect aspect){
        super(coordinates, Block.getIdFromBlock(TIBlocks.essentiaBlock));
        this.aspect = aspect;
    }

    public Aspect getAspect(){
        return aspect;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        aspect = Aspect.getAspect(tagCompound.getString("aspectTag"));
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setString("aspectTag", aspect.getTag());
    }
}
