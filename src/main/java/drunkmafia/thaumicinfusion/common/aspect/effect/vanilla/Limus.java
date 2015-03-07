package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 01/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("limus"), cost = 4)
public class Limus extends AspectEffect {

    public void onFallenUpon(World world, int x, int y, int z, Entity ent, float fall) {
        ent.motionY = ent.fallDistance;
    }

    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent) {
        ent.motionY = ent.fallDistance;
    }

}
