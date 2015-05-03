package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("perditio"), cost = 4)
public class Perditio extends AspectEffect {

    Random rand = new Random();

    @OverrideBlock
    public void onFallenUpon(World world, int x, int t, int z, Entity entity, float fall) {
        explode(world);
    }

    @OverrideBlock
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        explode(world);
    }

    @OverrideBlock
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        explode(world);
    }

    @OverrideBlock
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(world.isRemote)
            return true;

        explode(world);
        return true;
    }

    void explode(World world){
        if(rand.nextInt(20) == rand.nextInt(20))
            world.createExplosion(null, getPos().x, getPos().y, getPos().z, 4.0F, true);
    }
}
