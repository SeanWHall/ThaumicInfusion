package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "perfodio", cost = 2)
public class Perfodio extends AspectEffect {
    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return getHardness(world, x, y, z);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        return getHardness(world, x, y, z);
    }

    float getHardness(World world, int x, int y, int z){
        return 100;
    }
}
