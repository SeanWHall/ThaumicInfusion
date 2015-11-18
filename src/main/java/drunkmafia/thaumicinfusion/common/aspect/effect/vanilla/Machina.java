/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = "machina", cost = 2)
public class Machina extends AspectLink {

    @Override
    @BlockMethod
    public boolean shouldCheckWeakPower(IBlockAccess world, BlockPos pos, EnumFacing side) {
        World destinationWorld;
        WorldCoordinates destin = getDestination();
        if (destin == null || (destinationWorld = DimensionManager.getWorld(destin.dim)) == null || destinationWorld.isAirBlock(destin.pos))
            return false;

        TIWorldData worldData = TIWorldData.getWorldData(destinationWorld);
        BlockData data;
        boolean power = false;
        if ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX() - 1, destin.pos.getY(), destin.pos.getZ()), destin.dim))) == null || !data.hasEffect(Machina.class))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX() - 1, destin.pos.getY(), destin.pos.getZ())) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX() + 1, destin.pos.getY(), destin.pos.getZ()), destin.dim))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX() + 1, destin.pos.getY(), destin.pos.getZ())) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX(), destin.pos.getY() - 1, destin.pos.getZ()), destin.dim))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX(), destin.pos.getY() - 1, destin.pos.getZ())) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX(), destin.pos.getY() + 1, destin.pos.getZ()), destin.dim))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX(), destin.pos.getY() + 1, destin.pos.getZ())) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX(), destin.pos.getY(), destin.pos.getZ() - 1), destin.dim))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX(), destin.pos.getY(), destin.pos.getZ() - 1)) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoordinates(new BlockPos(destin.pos.getX(), destin.pos.getY(), destin.pos.getZ() + 1), destin.dim))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getStrongPower(new BlockPos(destin.pos.getX(), destin.pos.getY(), destin.pos.getZ() + 1)) > 0;

        return power;
    }

    @Override
    @BlockMethod
    public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    @BlockMethod
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        World destinationWorld;
        WorldCoordinates destin = getDestination();

        if (destin == null || (destinationWorld = DimensionManager.getWorld(destin.dim)) == null || state.getBlock().getClass() == getClass())
            return;

        destinationWorld.notifyNeighborsOfStateChange(destin.pos, this);
    }
}
