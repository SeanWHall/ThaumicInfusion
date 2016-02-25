/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = ("permutatio"), cost = 4)
public class Permutatio extends AspectLink implements IServerTickable {

    private boolean lastRedstoneSignal;

    @Override
    public void serverTick(World world) {
        BlockPos pos = getPos().pos;
        IBlockState state = world.getBlockState(pos);

        WorldCoordinates destin = getDestination();
        if (destin == null) return;

        World destinationWorld = DimensionManager.getWorld(destin.dim);
        boolean power = world.isBlockIndirectlyGettingPowered(pos) > 1;
        if (power != lastRedstoneSignal && state.getBlock().getMobilityFlag() == 0 && state.getBlock() != Blocks.bedrock && state.getBlock().getBlockHardness(world, pos) != -1.0F) {
            lastRedstoneSignal = power;

            IBlockState newBlock = destinationWorld.getBlockState(destin.pos);
            if (newBlock == null || (newBlock.getBlock().getMobilityFlag() == 0 && newBlock.getBlock() != Blocks.bedrock && newBlock.getBlock().getBlockHardness(world, pos) != -1.0F)) {
                TileEntity oldTile = world.getTileEntity(pos), newTile = destinationWorld.getTileEntity(destin.pos);

                destinationWorld.removeTileEntity(destin.pos);
                world.removeTileEntity(pos);

                destinationWorld.setBlockState(destin.pos, Blocks.air.getDefaultState());
                destinationWorld.setBlockState(destin.pos, state);

                world.setBlockState(pos, Blocks.air.getDefaultState());
                world.setBlockState(pos, newBlock);

                if (oldTile != null) {
                    destinationWorld.removeTileEntity(destin.pos);
                    oldTile.validate();
                    destinationWorld.setTileEntity(destin.pos, oldTile);
                }

                if (newTile != null) {
                    world.removeTileEntity(pos);
                    newTile.validate();
                    world.setTileEntity(pos, newTile);
                }
            }
        }
    }
}
