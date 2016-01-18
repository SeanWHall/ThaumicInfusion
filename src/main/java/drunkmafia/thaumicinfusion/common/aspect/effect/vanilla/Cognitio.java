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
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@Effect(aspect = "cognitio", cost = 1)
public class Cognitio extends AspectEffect {
    @BlockMethod()
    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return 1;
    }


    @SideOnly(Side.CLIENT)
    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random random) {
        for (int l = pos.getX() - 2; l <= maxY + 2; ++l) {
            for (int i1 = pos.getZ() - 2; i1 <= pos.getZ() + 2; ++i1) {
                if (l > pos.getX() - 2 && l < pos.getX() + 2 && i1 == pos.getZ() - 1)
                    i1 = pos.getZ() + 2;

                if (random.nextInt(16) == 0) {
                    for (int j1 = pos.getY(); j1 <= pos.getY() + 1; ++j1) {
                        if (world.getBlockState(new BlockPos(l, j1, i1)).getBlock() instanceof BlockEnchantmentTable) {
                            if (!world.isAirBlock(new BlockPos((l - pos.getX()) / 2 + pos.getX(), j1, (i1 - pos.getZ()) / 2 + pos.getZ())))
                                break;

                            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, (double) ((float) (l - pos.getX()) + random.nextFloat()) - 0.5D, (double) ((float) (j1 - pos.getY()) - random.nextFloat() - 1.0F), (double) ((float) (i1 - pos.getZ()) + random.nextFloat()) - 0.5D, (double) pos.getX() + 0.5D, (double) pos.getY() + 2.0D, (double) pos.getZ() + 0.5D);
                        }
                    }
                }
            }
        }
    }
}
