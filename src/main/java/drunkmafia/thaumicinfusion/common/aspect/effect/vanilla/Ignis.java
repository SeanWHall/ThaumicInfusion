/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@Effect(aspect = "ignis")
public class Ignis extends AspectEffect {

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public boolean shouldDrain() {
        return false;
    }

    @OverrideBlock
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 0;
    }

    @OverrideBlock
    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return true;
    }

    @OverrideBlock
    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 4096;
    }

    @OverrideBlock
    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }
}
