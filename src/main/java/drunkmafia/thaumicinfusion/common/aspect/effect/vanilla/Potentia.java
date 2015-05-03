package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("potentia"), cost = 4)
public class Potentia  extends AspectEffect {

    @OverrideBlock
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int p_149709_5_){
        int i1 = access.getBlockMetadata(x, y, z);
        return i1 == 5 && p_149709_5_ == 1 ? 0 : (i1 == 3 && p_149709_5_ == 3 ? 0 : (i1 == 4 && p_149709_5_ == 2 ? 0 : (i1 == 1 && p_149709_5_ == 5 ? 0 : (i1 == 2 && p_149709_5_ == 4 ? 0 : 15))));
    }

    @OverrideBlock
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side){
        return side == 0 ? this.isProvidingWeakPower(access, x, y, z, side) : 0;
    }
}
