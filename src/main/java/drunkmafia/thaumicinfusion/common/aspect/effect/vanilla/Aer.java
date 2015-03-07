package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "aer", cost = 2)
public class Aer extends AspectEffect {

    static long maxCooldown = 2000L;
    long cooldown;

    @Override
    public void updateBlock(World world) {
        if(world.isRemote)
            return;

        if(cooldown + maxCooldown < System.currentTimeMillis()) {
            AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 1, pos.z + 1).expand(1, 1, 1);
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

            for (EntityPlayer player : players)
                player.addPotionEffect(new PotionEffect(Potion.jump.getId(), 100));

            cooldown = System.currentTimeMillis();
        }


    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        getMaterial(world, x, y, z);
    }

    public Material getMaterial(World world, int x, int y, int z) {
        return getMaterial();
    }
}
