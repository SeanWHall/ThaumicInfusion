/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IInfusionStabiliser;

@Effect(aspect = "ordo", cost = 1)
public class Ordo extends AspectEffect implements IInfusionStabiliser {

    @Override
    @BlockMethod()
    public boolean canStabaliseInfusion(World world, BlockPos pos) {
        return true;
    }
}
