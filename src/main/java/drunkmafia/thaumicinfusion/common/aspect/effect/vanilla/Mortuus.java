/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "mortuus", cost = 4)
public class Mortuus extends AspectEffect {

    static final long maxCooldown = 2000L;
    private static int[] mobs = {
            50,
            51,
            52,
            54,
            55,
            58
    };
    long cooldown;

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
        if (world.isRemote || world.getBlockState(pos).getBlock().getLightValue() > 8 || !world.isAirBlock(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ())) || !world.isAirBlock(new BlockPos(pos.getX(), pos.getY() + 2, pos.getZ())))
            return;

        if (System.currentTimeMillis() > cooldown + maxCooldown && rand.nextInt(1000) == 1 && drainAspects(world, Aspect.DEATH)) {
            Entity entity = EntityList.createEntityByID(mobs[rand.nextInt(mobs.length)], world);
            entity.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
            world.spawnEntityInWorld(entity);
            cooldown = System.currentTimeMillis();
        }
    }

    public boolean drainAspects(World world, Aspect aspect) {
        int cost = AspectHandler.getCostOfEffect(aspect);
        for (int x = pos.pos.getX() - 10; x < pos.pos.getX() + 10; x++) {
            for (int y = pos.pos.getY() - 10; y < pos.pos.getY() + 10; y++) {
                for (int z = pos.pos.getZ() - 10; z < pos.pos.getZ() + 10; z++) {
                    TileEntity tileEntity = world.getTileEntity(pos.pos);
                    if (tileEntity instanceof IAspectSource) {
                        IAspectSource source = (IAspectSource) tileEntity;
                        if (source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            world.playSound((double) ((float) tileEntity.getPos().getX() + 0.5F), (double) ((float) tileEntity.getPos().getY() + 0.5F), (double) ((float) tileEntity.getPos().getZ() + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.forceBlockUpdateTick(state.getBlock(), pos, world.rand);
    }
}
