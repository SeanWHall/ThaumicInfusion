/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;

public class EssentiaData extends BlockSavable {

    private Aspect aspect;

    public EssentiaData(){}

    public EssentiaData(WorldCoordinates coordinates, Aspect aspect) {
        super(coordinates);
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
