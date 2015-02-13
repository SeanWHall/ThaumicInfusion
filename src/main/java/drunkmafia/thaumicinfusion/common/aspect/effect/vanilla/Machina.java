package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "machina", cost = 4)
public class Machina extends AspectEffect {

    boolean oldIsPowered, isPowered;

    @Override
    public void updateBlock(World world) {

        WorldCoord pos = getPos();
        isPowered = !world.isBlockIndirectlyGettingPowered(pos.x, pos.y, pos.z);
        if(isPowered != oldIsPowered){
            for(AspectEffect effect : data.getEffects())
                if(effect != this)
                    effect.isEnabled = isPowered;

            if(world.isRemote)
                Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);

            oldIsPowered = isPowered;
        }
    }
}
