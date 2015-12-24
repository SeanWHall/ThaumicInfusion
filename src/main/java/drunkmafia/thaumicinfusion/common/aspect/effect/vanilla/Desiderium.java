/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Effect(aspect = "desiderium", cost = 4)
public class Desiderium extends AspectEffect {

    private int range = 10, tickTime = 4;
    private Map<EntityItem, PathEntity> paths = new HashMap<EntityItem, PathEntity>();

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        range = config.getInt("The range that lucrum can find items to pull", "Desiderium", range, 1, 40, "");
        tickTime = config.getInt("Tick Time", "Desiderium", tickTime, 1, 20, "Delay before the effect ticks again");
    }


    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.pos, world.getBlockState(pos.pos), world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, rand);
        WorldCoordinates coord = getPos();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.fromBounds(coord.pos.getX(), coord.pos.getY(), coord.pos.getZ(), coord.pos.getX() + 1, coord.pos.getY() + 1, coord.pos.getZ() + 1).expand(10, 10, 10);
        ArrayList<EntityItem> list = (ArrayList<EntityItem>) world.getEntitiesWithinAABB(EntityItem.class, axisalignedbb);

        double speed = 0.05D;

        for (EntityItem item : list) {
            if (!isItemNearBlock(item)) {
                item.motionX = item.posX > coord.pos.getX() ? -speed : speed;
                item.motionZ = item.posZ > coord.pos.getZ() ? -speed : speed;
            }
        }
    }


    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entityIn) {
        world.forceBlockUpdateTick(world.getBlockState(pos).getBlock(), pos, world.rand);
    }

    boolean isItemNearBlock(EntityItem item) {
        return getPos().getDistanceSquared(new BlockPos((int) item.posX, (int) item.posY, (int) item.posZ)) < 1;
    }
}
