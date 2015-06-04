/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.world.IBlockAccess;

@Effect(aspect = ("lux"), cost = 1)
public class Lux extends AspectEffect {

    @OverrideBlock
    public int getLightValue(IBlockAccess world, int x, int y, int z){
        return 14;
    }
}
