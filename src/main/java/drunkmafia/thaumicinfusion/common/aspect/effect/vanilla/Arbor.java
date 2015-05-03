package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "arbor", cost = 2)
public class Arbor extends AspectEffect {

    @OverrideBlock
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
        setBlockBounds(0, 0, 0, 1, 0.5F, 1);
    }

    @OverrideBlock
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
    }
}
