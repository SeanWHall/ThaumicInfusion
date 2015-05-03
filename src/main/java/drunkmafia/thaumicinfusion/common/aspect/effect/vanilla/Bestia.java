package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.IBlockAccess;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "bestia", cost = 4)
public class Bestia extends AspectEffect {
    @OverrideBlock
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }
}
