package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 13/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "cognitio", cost = 1)
public class Cognitio extends AspectEffect {
    @OverrideBlock()
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return 1;
    }
}
