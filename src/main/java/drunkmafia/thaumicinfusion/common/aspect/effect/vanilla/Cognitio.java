/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@Effect(aspect = "cognitio", cost = 1)
public class Cognitio extends AspectEffect {
    @BlockMethod()
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @BlockMethod(overrideBlockFunc = false)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        for (int l = x - 2; l <= maxY + 2; ++l) {
            for (int i1 = z - 2; i1 <= z + 2; ++i1) {
                if (l > x - 2 && l < x + 2 && i1 == z - 1)
                    i1 = z + 2;

                if (random.nextInt(16) == 0) {
                    for (int j1 = y; j1 <= y + 1; ++j1) {
                        if (world.getBlockState(new BlockPos(l, j1, i1)).getBlock() instanceof BlockEnchantmentTable) {
                            if (!world.isAirBlock(new BlockPos((l - x) / 2 + x, j1, (i1 - z) / 2 + z)))
                                break;

                            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, (double) ((float) (l - x) + random.nextFloat()) - 0.5D, (double) ((float) (j1 - y) - random.nextFloat() - 1.0F), (double) ((float) (i1 - z) + random.nextFloat()) - 0.5D, (double) x + 0.5D, (double) y + 2.0D, (double) z + 0.5D);
                        }
                    }
                }
            }
        }
    }
}
