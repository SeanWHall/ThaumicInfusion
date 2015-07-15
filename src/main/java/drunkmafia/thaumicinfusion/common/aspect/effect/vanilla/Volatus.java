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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.WorldCoordinates;

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
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), tickTime);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), tickTime);
        WorldCoordinates pos = getPos();
        if (!world.isAirBlock(pos.x, pos.y + 1, pos.z))
            return;

        float size = getSize(world);

        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + size, pos.z + 1);
        List list = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

        for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
            if (player == null) continue;
            int playerHash = player.getCommandSenderName().hashCode();

            if (list.contains(player)) {
                isFlying.add(playerHash);

                player.capabilities.isFlying = true;
                player.sendPlayerAbilities();
            } else if (isFlying.contains(playerHash)) {
                if (isPlayerAboveVolatusBlock(size, player))
                    return;
                isFlying.remove((Integer) playerHash);

                player.capabilities.isFlying = false;
                player.sendPlayerAbilities();
            }
        }
    }

    boolean isPlayerAboveVolatusBlock(float size, EntityPlayer player){
        for(int y = 0; y < size; y++) {
            int posX = (int) player.posX, posY = (int) (player.posY - y), posZ = (int) player.posZ;
            if(! player.worldObj.isAirBlock(posX, posY, posZ)) {
                BlockData data = TIWorldData.getWorldData(player.worldObj).getBlock(BlockData.class, new WorldCoordinates(posX, posY, posZ, player.dimension));
                if (data != null)
                    return true;
            }else
                break;
        }
        return false;
    }

    float getSize(World world){
        WorldCoordinates pos = getPos();
        float ret = defSize;
        int curretY = pos.y - 1;
        while(!world.isAirBlock(pos.x, curretY, pos.z)){
            BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, new WorldCoordinates(pos.x, curretY, pos.z, world.provider.dimensionId));
            if (data != null && data.hasEffect(Volatus.class)) {
                ret += defSize;
                curretY--;
            }else break;
        }
        return ret;
    }
}
