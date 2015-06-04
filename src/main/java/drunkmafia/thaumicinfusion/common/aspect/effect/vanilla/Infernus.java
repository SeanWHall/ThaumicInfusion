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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

@Effect(aspect = ("infernus"), cost = 4)
public class Infernus extends AspectEffect {

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent) {
        setOnFire(ent);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onFallenUpon(World world, int x, int y, int z, Entity ent, float fall) {
        setOnFire(ent);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityWalking(World world, int x, int y, int z, Entity ent) {
        setOnFire(ent);
    }

    public void setOnFire(Entity ent){
        if(!(ent instanceof EntityLivingBase))
            return;
        ent.setFire(8);
    }
}
