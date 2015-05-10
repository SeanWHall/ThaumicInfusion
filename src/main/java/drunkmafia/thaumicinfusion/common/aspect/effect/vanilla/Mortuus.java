package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "mortuus", cost = 4)
public class Mortuus extends AspectEffect {

    static final long maxCooldown = 2000L;
    private static int[] mobs = {
            50,
            51,
            52,
            54,
            55,
            58
    };
    long cooldown;

    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        WorldCoord pos = getPos();
        if(world.isRemote || world.getBlockLightValue(pos.x, pos.y, pos.z) > 8 || !world.isAirBlock(pos.x, pos.y + 1, pos.z) || !world.isAirBlock(pos.x, pos.y + 2, pos.z))
            return;

        Random rand = world.rand;
        if(System.currentTimeMillis() > cooldown + maxCooldown && rand.nextInt(1000) == 1){
            Entity entity = EntityList.createEntityByID(mobs[rand.nextInt(mobs.length)], world);
            entity.setPosition(pos.x, pos.y + 1, pos.z);
            world.spawnEntityInWorld(entity);
            cooldown = System.currentTimeMillis();
        }
    }
}
