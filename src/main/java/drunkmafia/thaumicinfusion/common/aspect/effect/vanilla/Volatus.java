package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import java.util.List;
import java.util.Random;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "volatus", cost = 4)
public class Volatus extends AspectEffect {

    int defSize = 10, tickTime = 1;
    boolean isFlying;

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        defSize = config.getInt("Default Flying Range", "Volatus", defSize, 1, 100, "");
        tickTime = config.getInt("Tick Time", "Volatus", tickTime, 1, 20, "Delay before the effect ticks again");
    }

    @Override
    public void aspectInit(World world, WorldCoord pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);

        WorldCoord pos = getPos();

        if(!world.isRemote && !world.isAirBlock(pos.x, pos.y + 1, pos.z))
            return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if(player == null || player.capabilities.isCreativeMode)
            return;

        float size = getSize(world);

        AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + size, pos.z + 1);
        List list = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

        if(list.contains(player)) {
            isFlying = true;
            player.capabilities.isFlying = true;
        }else if(isFlying) {
            if(isPlayerAboveVolatusBlock(size, player))
                return;

            player.capabilities.isFlying = false;
            player.sendPlayerAbilities();
            isFlying = false;
        }
    }

    boolean isPlayerAboveVolatusBlock(float size, EntityPlayer player){
        for(int y = 0; y < size; y++) {
            int posX = (int) player.posX, posY = (int) (player.posY - y), posZ = (int) player.posZ;
            if(! player.worldObj.isAirBlock(posX, posY, posZ)) {
                BlockData data = TIWorldData.getData(BlockData.class, player.worldObj, new WorldCoord(posX, posY, posZ));
                if (data != null)
                    return true;
            }else
                break;
        }
        return false;
    }

    float getSize(World world){
        WorldCoord pos = getPos();
        float ret = defSize;
        int curretY = pos.y - 1;
        while(!world.isAirBlock(pos.x, curretY, pos.z)){
            BlockData data = TIWorldData.getData(BlockData.class, world, new WorldCoord(pos.x, curretY, pos.z));
            if (data != null && data.hasEffect(Volatus.class)) {
                ret += defSize;
                curretY--;
            }else break;
        }
        return ret;
    }
}
