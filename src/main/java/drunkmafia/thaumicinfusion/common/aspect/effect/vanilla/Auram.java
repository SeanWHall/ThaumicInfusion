package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = ("auram"), cost = 1)
public class Auram extends AspectEffect {

    private ForgeChunkManager.Ticket ticket;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote) loadChunk(world, pos.pos);
    }

    @Override
    public void onRemoveEffect() {
        unloadChunk(pos.pos);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) loadChunk(world, pos);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) unloadChunk(pos);
    }

    private void loadChunk(World world, BlockPos pos) {
        this.ticket = ForgeChunkManager.requestTicket(ThaumicInfusion.instance, world, ForgeChunkManager.Type.NORMAL);
        if (this.ticket != null)
            ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(pos.getX() / 16, pos.getZ() / 16));
    }

    private void unloadChunk(BlockPos pos) {
        if (this.ticket != null)
            ForgeChunkManager.unforceChunk(ticket, new ChunkCoordIntPair(pos.getX() / 16, pos.getZ() / 16));
    }
}
