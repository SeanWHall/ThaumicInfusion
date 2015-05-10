package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;

import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "praecantatio", cost = 2)
public class Praecantatio extends AspectEffect {
    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (!world.isRemote)
            return;
        WorldCoord pos = getPos();
        Random rand = world.rand;

        x = pos.x + (rand.nextInt(10) - 5);
        y = pos.y + (rand.nextInt(10) - 5);
        z = pos.z + (rand.nextInt(10) - 5);
        Thaumcraft.proxy.sparkle(x, y, z, rand.nextFloat(), 0, 0.1F);
    }
}
