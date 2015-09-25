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
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;

@Effect(aspect = "alienis")
public class Alienis extends AspectEffect {

    private int size = 10;

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        size = config.getInt("Size of random tp", "Alienis", size, 1, 50, "");
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if(!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase)entity);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        if(!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase)entity);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float dist) {
        if(!world.isRemote && entity instanceof EntityLivingBase)
            warpEntity(world, (EntityLivingBase)entity);
    }

    public void warpEntity(World world, EntityLivingBase entity){
        ChunkCoordinates[] possibleCoords = getPossibleWarps(world);
        if(possibleCoords == null || possibleCoords.length == 0)
            return;
        ChunkCoordinates warp = possibleCoords[world.rand.nextInt(possibleCoords.length)];
        entity.setPositionAndUpdate(warp.posX + 0.5D, warp.posY, warp.posZ + 0.5D);
    }

    public ChunkCoordinates[] getPossibleWarps(World world){
        WorldCoordinates pos = getPos();
        ArrayList<ChunkCoordinates> warps = new ArrayList<ChunkCoordinates>();
        for (int x = -size + pos.x; x < size + pos.x; x++){
            for (int y = -size + pos.y; y < size + pos.y; y++){
                for (int z = -size + pos.z; z < size + pos.z; z++){
                    if(!world.isAirBlock(x, y - 1, z) && world.isAirBlock(x, y, z) && world.isAirBlock(x, y + 1, z))
                        warps.add(new ChunkCoordinates(x, y, z));
                }
            }
        }
        ChunkCoordinates[] retWarps = new ChunkCoordinates[warps.size()];
        warps.toArray(retWarps);
        return retWarps;
    }
}
