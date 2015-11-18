/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;
import java.util.Random;

@Effect(aspect = ("perditio"), cost = 4)
public class Perditio extends AspectEffect {

    Random rand = new Random();

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
        if (world.isRemote)
            return;

        if (pos == null || world.isAirBlock(pos))
            return;

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for (EntityPlayer ent : ents) {
            if (!ent.isSneaking()) {
                explode(world);
                return;
            }
        }
    }

    void explode(World world) {
        if (rand.nextInt(20) == rand.nextInt(20) && !world.isRemote) {
            world.createExplosion(null, getPos().pos.getX(), getPos().pos.getY(), getPos().pos.getZ(), 4.0F, true);
            TIWorldData worldData = TIWorldData.getWorldData(world);
            BlockData data = worldData.getBlock(BlockData.class, getPos());
            if (data != null) {
                data.removeEffect(getClass());
                if (data.getEffects().length == 0) worldData.removeData(BlockData.class, getPos(), true);
            }
        }
    }
}
