package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("tutamen"), cost = 1)
public class Tutamen extends AspectEffect {

    public static float hardnessModifer = 50F;

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        BlockData data = TIWorldData.getData(BlockData.class, world, getPos());
        return data.getContainingBlock().getBlockHardness(world, x, y, z) + hardnessModifer;
    }
}
