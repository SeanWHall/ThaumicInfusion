/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;

@Effect(aspect = "auram", cost = 1)
public class Auram extends AspectEffect {

    @Override
    public boolean shouldDrain() {
        return false;
    }

    @Override
    public int getCost() {
        return 1;
    }
}
