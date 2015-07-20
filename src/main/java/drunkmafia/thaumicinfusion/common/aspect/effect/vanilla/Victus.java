/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;

import java.util.Random;

@Effect(aspect = "victus", cost = 4)
public class Victus extends AspectEffect {

    static final long maxCooldown = 2000L;
    private static int[] mobs = {
            90,
            91,
            92,
            93
    };
    long cooldown;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        WorldCoordinates pos = getPos();
        if(world.isRemote || world.getBlockLightValue(pos.x, pos.y, pos.z) < 8 || !world.isAirBlock(pos.x, pos.y + 1, pos.z) || !world.isAirBlock(pos.x, pos.y + 2, pos.z))
            return;

        Random rand = world.rand;
        if (System.currentTimeMillis() > cooldown + maxCooldown && rand.nextInt(1000) == 1 && drainAspects(world, Aspect.LIFE)) {
            Entity entity = EntityList.createEntityByID(mobs[rand.nextInt(mobs.length)], world);
            entity.setPosition(pos.x, pos.y + 1, pos.z);
            world.spawnEntityInWorld(entity);
            cooldown = System.currentTimeMillis();
        }
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        return false;
    }

    public boolean drainAspects(World world, Aspect aspect) {
        int cost = AspectHandler.getCostOfEffect(aspect);
        for (int x = pos.x - 10; x < pos.x + 10; x++) {
            for (int y = pos.y - 10; y < pos.y + 10; y++) {
                for (int z = pos.z - 10; z < pos.z + 10; z++) {
                    TileEntity tileEntity = world.getTileEntity(x, y, z);
                    if (tileEntity instanceof IAspectSource) {
                        IAspectSource source = (IAspectSource) tileEntity;
                        if (source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            world.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
