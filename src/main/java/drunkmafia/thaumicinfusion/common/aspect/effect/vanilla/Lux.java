package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.world.IBlockAccess;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */

@Effect(aspect = ("lux"), cost = 1)
public class Lux extends AspectEffect {

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z){
        return 14;
    }
}
