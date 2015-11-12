/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Effect(aspect = "volatus")
public class Volatus extends AspectEffect {

    private final List<Integer> isFlying = new ArrayList<Integer>();
    private int defSize = 10, tickTime = 1;

    @Override
    public int getCost() {
        return 8;
    }

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        this.defSize = config.getInt("Default Flying Range", "Volatus", this.defSize, 1, 100, "");
        this.tickTime = config.getInt("Tick Time", "Volatus", this.tickTime, 1, 20, "Delay before the effect ticks again");
    }

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
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

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), this.tickTime);
        WorldCoordinates pos = this.getPos();
        if (!world.isAirBlock(pos.x, pos.y + 1, pos.z))
            return;

        float size = this.getSize(world);

        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + size, pos.z + 1);
        List list = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

        for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
            if (player == null) continue;
            int playerHash = player.getCommandSenderName().hashCode();

            if (list.contains(player)) {
                this.isFlying.add(playerHash);

                player.capabilities.isFlying = true;
                player.sendPlayerAbilities();
            } else if (this.isFlying.contains(playerHash)) {
                if (this.isPlayerAboveVolatusBlock(size, player))
                    return;
                this.isFlying.remove((Integer) playerHash);

                player.capabilities.isFlying = false;
                player.sendPlayerAbilities();
            }
        }
    }

    boolean isPlayerAboveVolatusBlock(float size, EntityPlayer player) {
        for (int y = 0; y < size; y++) {
            int posX = (int) player.posX, posY = (int) (player.posY - y), posZ = (int) player.posZ;
            if (!player.worldObj.isAirBlock(posX, posY, posZ)) {
                BlockData data = TIWorldData.getWorldData(player.worldObj).getBlock(BlockData.class, new WorldCoordinates(posX, posY, posZ, player.dimension));
                if (data != null)
                    return true;
            } else
                break;
        }
        return false;
    }

    float getSize(World world) {
        WorldCoordinates pos = this.getPos();
        float ret = this.defSize;
        int curretY = pos.y - 1;
        while (!world.isAirBlock(pos.x, curretY, pos.z)) {
            BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, new WorldCoordinates(pos.x, curretY, pos.z, world.provider.dimensionId));
            if (data != null && data.hasEffect(Volatus.class)) {
                ret += this.defSize;
                curretY--;
            } else break;
        }
        return ret;
    }
}
