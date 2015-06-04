/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.List;
import java.util.Random;

@Effect(aspect = "fames", cost = 4)
public class Fames extends AspectEffect {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if(!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, new Random());
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(15.0D, 15.0D, 15.0D));
        for(EntityPlayer player : players) {
            FoodStats food = player.getFoodStats();
            if (food.getFoodLevel() > 0 && random.nextBoolean())
                food.setFoodLevel(food.getFoodLevel() - 1);
        }
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 50);
    }
}
