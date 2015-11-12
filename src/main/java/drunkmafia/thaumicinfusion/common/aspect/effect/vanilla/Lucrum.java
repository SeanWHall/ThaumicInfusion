/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Effect(aspect = "lucrum")
public class Lucrum extends AspectEffect {

    private final Map<EntityItem, PathEntity> paths = new HashMap<EntityItem, PathEntity>();
    private int range = 10, tickTime = 4;

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        this.range = config.getInt("The range that lucrum can find items to pull", "Lucrum", this.range, 1, 40, "");
        this.tickTime = config.getInt("Tick Time", "Lucrum", this.tickTime, 1, 20, "Delay before the effect ticks again");
    }

    @Override
    public int getCost() {
        return 4;
    }

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
        WorldCoordinates coord = this.getPos();
        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(coord.x, coord.y, coord.z, coord.x + 1, coord.y + 1, coord.z + 1).expand(10, 10, 10);
        ArrayList<EntityItem> list = (ArrayList<EntityItem>) world.getEntitiesWithinAABB(EntityItem.class, axisalignedbb);

        double speed = 0.05D;

        for (EntityItem item : list) {
            if (!this.isItemNearBlock(item)) {
                item.motionX = item.posX > coord.x ? -speed : speed;
                item.motionZ = item.posZ > coord.z ? -speed : speed;
            }
        }
    }


    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
        return false;
    }

    boolean isItemNearBlock(EntityItem item) {
        return this.getPos().getDistanceSquared((int) item.posX, (int) item.posY, (int) item.posZ) < 1;
    }
}
