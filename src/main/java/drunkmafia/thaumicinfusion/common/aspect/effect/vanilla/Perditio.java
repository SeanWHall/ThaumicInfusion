/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.Random;

@Effect(aspect = ("perditio"))
public class Perditio extends AspectEffect {

    Random rand = new Random();

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @Override
    public int getCost() {
        return 4;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote)
            return;

        WorldCoordinates pos = getPos();
        if (pos == null || world.isAirBlock(pos.x, pos.y, pos.z))
            return;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 2, pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for (EntityPlayer ent : ents) {
            if (!ent.isSneaking()) {
                explode(world);
                return;
            }
        }
    }

    void explode(World world){
        if (rand.nextInt(20) == rand.nextInt(20) && !world.isRemote) {
            world.createExplosion(null, getPos().x, getPos().y, getPos().z, 4.0F, true);
            TIWorldData worldData = TIWorldData.getWorldData(world);
            BlockData data = worldData.getBlock(BlockData.class, getPos());
            if (data != null) {
                data.removeEffect(getClass());
                if (data.getEffects().length == 0) worldData.removeData(BlockData.class, getPos(), true);
            }
        }
    }
}
