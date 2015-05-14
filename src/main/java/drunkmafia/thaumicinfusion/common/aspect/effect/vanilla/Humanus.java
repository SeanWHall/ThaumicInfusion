package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "humanus", cost = 4)
public class Humanus extends AspectEffect {

    private EntityPlayer target;

    @Override
    public void interactWithEntity(EntityPlayer player, Entity target) {
        if (player != null && target != null && target instanceof EntityPlayer && player.getDistance(pos.x, pos.y, pos.z) < 3) {
            player.openGui(ThaumicInfusion.instance, 0, player.worldObj, pos.x, pos.y, pos.z);
            this.target = (EntityPlayer) target;
        }
    }

    public EntityPlayer getTarget() {
        EntityPlayer player = target;
        target = null;
        return player;
    }
}
