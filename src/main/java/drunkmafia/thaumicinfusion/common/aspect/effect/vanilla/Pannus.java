/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Effect(aspect = ("pannus"), cost = 1)
public class Pannus extends AspectEffect {

    @OverrideBlock(overrideBlockFunc = false)
    public void onFallenUpon(World world, int x, int y, int z, Entity ent, float fall) {
        ent.fallDistance = 0;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent) {
        ent.fallDistance = 0;
    }
}
