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

@Effect(aspect = "vitreus")
public class Vitreus extends AspectEffect {

    @Override
    public int getCost() {
        return 2;
    }

    @OverrideBlock
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return 0;
    }
}
