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
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;

@Effect(aspect = "alienis", cost = 2)
public class Alienis extends AspectEffect {

    private int size = 10;

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        size = config.getInt("Size of random tp", "Alienis", size, 1, 50, "");
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase) entity);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase) entity);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float dist) {
        if (!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase) entity);
    }

    public void warpEntity(World world, EntityLivingBase entity) {
        WorldCoordinates[] possibleCoords = getPossibleWarps(world);
        if (possibleCoords == null || possibleCoords.length == 0)
            return;
        WorldCoordinates warp = possibleCoords[world.rand.nextInt(possibleCoords.length)];
        entity.setPositionAndUpdate(warp.pos.getX() + 0.5D, warp.pos.getY(), warp.pos.getZ() + 0.5D);
    }

    public WorldCoordinates[] getPossibleWarps(World world) {
        WorldCoordinates pos = getPos();
        ArrayList<BlockPos> warps = new ArrayList<BlockPos>();
        for (int x = -size + pos.pos.getX(); x < size + pos.pos.getX(); x++) {
            for (int y = -size + pos.pos.getY(); y < size + pos.pos.getY(); y++) {
                for (int z = -size + pos.pos.getZ(); z < size + pos.pos.getZ(); z++) {
                    if (!world.isAirBlock(new BlockPos(x, y - 1, z)) && world.isAirBlock(new BlockPos(x, y, z)) && world.isAirBlock(new BlockPos(x, y + 1, z)))
                        warps.add(new BlockPos(x, y, z));
                }
            }
        }
        WorldCoordinates[] retWarps = new WorldCoordinates[warps.size()];
        warps.toArray(retWarps);
        return retWarps;
    }
}
