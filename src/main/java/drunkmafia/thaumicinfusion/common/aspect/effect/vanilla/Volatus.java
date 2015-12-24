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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Effect(aspect = "volatus", cost = 4)
public class Volatus extends AspectEffect {

    private int defSize = 10, tickTime = 1;
    private List<Integer> isFlying = new ArrayList<Integer>();

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        defSize = config.getInt("Default Flying Range", "Volatus", defSize, 1, 100, "");
        tickTime = config.getInt("Tick Time", "Volatus", tickTime, 1, 20, "Delay before the effect ticks again");
    }

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        world.scheduleUpdate(pos, state.getBlock(), 1);

        if (!world.isAirBlock(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())))
            return;

        float size = getSize(world);

        AxisAlignedBB axisalignedbb = AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + size, pos.getZ() + 1);
        List playersInAABB = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

        for (EntityPlayer worldPlayer : (List<EntityPlayer>) world.playerEntities) {
            if (worldPlayer == null) continue;
            int playerHash = worldPlayer.getName().hashCode();

            if (playersInAABB.contains(worldPlayer)) {
                if (!isFlying.contains(playerHash))
                    isFlying.add(playerHash);

                worldPlayer.capabilities.isFlying = true;
                worldPlayer.sendPlayerAbilities();
                return;
            } else if (isFlying.contains(playerHash)) {
                isFlying.remove((Integer) playerHash);

                if (!isPlayerAboveVolatusBlock(size, worldPlayer)) {
                    worldPlayer.capabilities.isFlying = false;
                    worldPlayer.sendPlayerAbilities();
                }
                return;
            }
        }
    }

    private boolean isPlayerAboveVolatusBlock(float size, EntityPlayer player) {
        int posX = (int) Math.floor(player.posX), posZ = (int) Math.floor(player.posZ);
        TIWorldData worldData = TIWorldData.getWorldData(player.worldObj);
        for (int y = 0; y < size; y++) {
            int posY = (int) (player.posY - y);
            BlockPos pos = new BlockPos(posX, posY, posZ);
            if (!player.worldObj.isAirBlock(pos)) {
                BlockData data = worldData.getBlock(BlockData.class, new WorldCoordinates(pos, player.dimension));
                if (data != null)
                    return data.hasEffect(Volatus.class);
            }
        }
        return false;
    }

    private float getSize(World world) {
        BlockPos pos = getPos().pos;
        float ret = defSize;
        int curretY = pos.getY() - 1;
        while (!world.isAirBlock(new BlockPos(pos.getX(), curretY, pos.getZ()))) {
            BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, new WorldCoordinates(new BlockPos(pos.getX(), curretY, pos.getZ()), world.provider.getDimensionId()));
            if (data != null && data.hasEffect(Volatus.class)) {
                ret += defSize;
                curretY--;
            } else break;
        }
        return ret;
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        updateTick(world, pos, state, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entityIn) {
        updateTick(world, pos, world.getBlockState(pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        updateTick(world, pos, state, world.rand);
    }
}
