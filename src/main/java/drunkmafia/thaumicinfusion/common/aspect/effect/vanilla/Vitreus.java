/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

@Effect(aspect = "vitreus", cost = 2)
public class Vitreus extends AspectEffect {
    @Override
    @BlockMethod
    public int getLightOpacity(IBlockAccess world, BlockPos pos) {
        return 0;
    }
}
