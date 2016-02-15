package drunkmafia.thaumicinfusion.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialTransparent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class FakeAirBlock extends Block {

    private boolean shouldCollide;

    protected FakeAirBlock(boolean shouldCollide) {
        super(new MaterialTransparent(MapColor.airColor));
        setBlockUnbreakable();

        this.shouldCollide = shouldCollide;
        if (!shouldCollide) setBlockBounds(0, 0, 0, 0, 0, 0);
    }

    public int getRenderType() {
        return -1;
    }

    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return shouldCollide ? super.getCollisionBoundingBox(worldIn, pos, state) : null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return !shouldCollide;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return shouldCollide;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public boolean isReplaceable(World worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {
        return true;
    }

    @Override
    public boolean addLandingEffects(WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return true;
    }
}
