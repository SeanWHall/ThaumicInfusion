/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;

@Effect(aspect = ("motus"), cost = 4)
public class Motus extends AspectLink implements IServerTickable {

    @Override
    public void serverTick(World world) {
        if (world.isRemote)
            return;

        WorldCoordinates pos = getPos();
        if (pos == null || world.isAirBlock(pos.pos) || pos.dim == 1)
            return;

        WorldCoordinates destin = getDestination();

        if (destin == null)
            return;

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ(), pos.pos.getX() + 1, pos.pos.getY() + 2, pos.pos.getZ() + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for (EntityPlayer ent : ents) {
            if (ent.isSneaking()) {
                World destWorld;
                if ((destWorld = DimensionManager.getWorld(destination.dim)) == null || destWorld.isAirBlock(destin.pos))
                    return;

                if (destin.dim != ent.worldObj.provider.getDimensionId())
                    ent.travelToDimension(destin.dim);

                ent.setPositionAndUpdate(destin.pos.getX() + 0.5F, destin.pos.getY() + 1F, destin.pos.getZ() + 0.5F);
                ent.setSneaking(false);
            }
        }
    }
}
