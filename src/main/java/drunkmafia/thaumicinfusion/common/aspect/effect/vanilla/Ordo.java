/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IInfusionStabiliser;

@Effect(aspect = "ordo", cost = 1)
public class Ordo extends AspectEffect implements IInfusionStabiliser {
    @Override
    public boolean canStabaliseInfusion(World world, int x, int y, int z) {
        return true;
    }
}
