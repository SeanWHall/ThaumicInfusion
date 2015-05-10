package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.world.IBlockAccess;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "potentia", cost = 1)
public class Potentia extends AspectEffect {

    @OverrideBlock
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side) {
        return 15;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int side) {
        return isProvidingWeakPower(access, x, y, z, side);
    }

    @OverrideBlock
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @OverrideBlock
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }
}
