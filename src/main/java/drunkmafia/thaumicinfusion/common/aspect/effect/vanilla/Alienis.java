package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "alienis", cost = 1)
public class Alienis extends AspectEffect {

    private int size = 10;

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float htiX, float hitY, float hitZ) {
        if(world.isRemote)
            return true;
        warpEntity(world, player);
        return true;
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
        WorldCoord pos = getPos();
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
