/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Effect(aspect = ("infernus"), cost = 4)
public class Infernus extends AspectEffect {

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn) {
        setOnFire(entityIn);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        setOnFire(entityIn);
    }

    public void setOnFire(Entity ent) {
        if (!(ent instanceof EntityLivingBase))
            return;
        ent.setFire(8);
    }
}
