/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@Effect(aspect = "humanus", cost = 4)
public class Humanus extends AspectEffect {

    @Override
    @BlockMethod()
    public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    @BlockMethod()
    public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    @BlockMethod()
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    @BlockMethod()
    public boolean canReplace(World worldIn, BlockPos pos, EnumFacing side, ItemStack stack) {
        return false;
    }

    @Override
    @BlockMethod()
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }

    @Override
    @BlockMethod()
    public boolean isReplaceable(World worldIn, BlockPos pos) {
        return false;
    }

    @Override
    @BlockMethod()
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    @BlockMethod()
    public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @BlockMethod()
    public float getBlockHardness(World worldIn, BlockPos pos) {
        return -1;
    }

    @Override
    @BlockMethod()
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
    }
}
