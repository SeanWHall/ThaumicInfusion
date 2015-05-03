package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("vinculum"), cost = 4)
public class Vinculum extends AspectEffect {
    
    public void aspectInit(World world,WorldCoord pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, new Random());
    }

    @OverrideBlock
    public void updateTick(World world, int x, int y, int z, Random rand) {
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 3, z + 1);
        ArrayList<EntityLivingBase> entities = (ArrayList<EntityLivingBase>) world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

        for(EntityLivingBase ent : entities) {
            ent.motionX = 0;
            ent.motionY = 0;
            ent.motionZ = 0;
        }
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
