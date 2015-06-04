/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.Random;

@Effect(aspect = ("iter"), cost = 4)
public class Iter extends AspectLink {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if(world.isRemote)
            return;
        WorldCoordinates pos = getPos();
        if (pos == null || world.isAirBlock(pos.x, pos.y, pos.z))
            return;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 2, pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for(EntityPlayer ent : ents) {
            if (ent.isSneaking()) {
                WorldCoordinates destin = getDestination();

                World destWorld;
                if (destin == null || (destWorld = DimensionManager.getWorld(destin.dim)) == null || destWorld.isAirBlock(destin.x, destin.y, destin.z))
                    return;

                if(destin.dim != ent.worldObj.provider.dimensionId)
                    ent.travelToDimension(destin.dim);
                ent.setPositionAndUpdate(destin.x + 0.5F, destin.y + 1F, destin.z + 0.5F);
                ent.setSneaking(false);
            }
        }
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
