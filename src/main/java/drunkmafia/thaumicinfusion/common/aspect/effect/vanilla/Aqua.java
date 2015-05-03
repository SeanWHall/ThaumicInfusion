package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("aqua"), cost = 1)
public class Aqua extends AspectEffect {

    public boolean isReplaceable(IBlockAccess access, int x, int y, int z) {
        return true;
    }

    @OverrideBlock
    public Material getMaterial() {
        return Material.water;
    }
}
