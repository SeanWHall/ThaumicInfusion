/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

@Effect(aspect = ("vinculum"), cost = 4)
public class Vinculum extends AspectEffect implements IServerTickable {

    @Override
    public void serverTick(World world) {
        if (world.isRemote) return;

        BlockPos blockPos = pos.pos;
        AxisAlignedBB axisalignedbb = AxisAlignedBB.fromBounds(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 3, blockPos.getZ() + 1);
        ArrayList<EntityLivingBase> entities = (ArrayList<EntityLivingBase>) world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

        for (EntityLivingBase ent : entities) {
            ent.motionX = 0;
            ent.motionY = 0;
            ent.motionZ = 0;
        }
    }
}
