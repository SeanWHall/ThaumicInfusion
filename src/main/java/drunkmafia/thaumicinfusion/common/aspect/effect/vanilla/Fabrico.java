package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "fabrico", cost = 4)
public class Fabrico extends AspectEffect {
    @Override
    public boolean shouldRegister() {
        return false;
    }
}
