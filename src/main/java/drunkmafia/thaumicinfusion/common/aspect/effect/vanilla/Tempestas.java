package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by DrunkMafia on 06/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("tempestas"), cost = 1)
public class Tempestas extends AspectEffect {

    @OverrideBlock(overrideBlockFunc = false)
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, new Random());
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        if(world.isRaining() && world.canBlockSeeTheSky(x, y, z))
            if (rand.nextInt(50) == rand.nextInt(50))
                world.spawnEntityInWorld(new EntityLightningBolt(world, x, y + 1, z));
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 50);
    }
}
