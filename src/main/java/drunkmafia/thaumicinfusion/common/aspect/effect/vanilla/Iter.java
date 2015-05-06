package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by DrunkMafia on 05/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("iter"), cost = 4)
public class Iter extends AspectLink {

    public static long maxCooldown = 5000L;
    long startCooldown;

    @Override
    public void updateBlock(World world) {
        if(world.isRemote)
            return;
        WorldCoord pos = getPos();

        if (pos == null || startCooldown > System.currentTimeMillis() || world.isAirBlock(pos.x, pos.y, pos.z))
            return;

        WorldCoord destin = getDestination();
        if (destin == null || world.isAirBlock(destin.x, destin.y, destin.z))
            return;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 2, pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for(EntityPlayer ent : ents) {
            if (ent.isSneaking()) {
                startCooldown = System.currentTimeMillis() + maxCooldown;

                TIWorldData.getData(BlockData.class, getDestinationWorld(), destin).getEffect(Iter.class).startCooldown = startCooldown;
                if(destin.dim != ent.worldObj.provider.dimensionId)
                    ent.travelToDimension(destin.dim);
                ent.setPositionAndUpdate(destin.x + 0.5F, destin.y + 1F, destin.z + 0.5F);
            }
        }
    }
}
