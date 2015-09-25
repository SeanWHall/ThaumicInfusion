/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.world.World;

import java.util.Random;

@Effect(aspect = "cognitio")
public class Cognitio extends AspectEffect {
    @OverrideBlock()
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @OverrideBlock(overrideBlockFunc = false)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        for (int l = x - 2; l <= maxY + 2; ++l) {
            for (int i1 = z - 2; i1 <= z + 2; ++i1) {
                if (l > x - 2 && l < x + 2 && i1 == z - 1)
                    i1 = z + 2;

                if (random.nextInt(16) == 0) {
                    for (int j1 = y; j1 <= y + 1; ++j1) {
                        if (world.getBlock(l, j1, i1) instanceof BlockEnchantmentTable) {
                            if (!world.isAirBlock((l - x) / 2 + x, j1, (i1 - z) / 2 + z))
                                break;

                            world.spawnParticle("enchantmenttable", (double) ((float) (l - x) + random.nextFloat()) - 0.5D, (double) ((float) (j1 - y) - random.nextFloat() - 1.0F), (double) ((float) (i1 - z) + random.nextFloat()) - 0.5D, (double) x + 0.5D, (double) y + 2.0D, (double) z + 0.5D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getCost() {
        return 1;
    }
}
