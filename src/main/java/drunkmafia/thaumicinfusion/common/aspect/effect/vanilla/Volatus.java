package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by DrunkMafia on 12/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "volatus", cost = 4)
public class Volatus extends AspectEffect {

    int defSize = 10;
    boolean isFlying;

    @Override
    @SideOnly(Side.CLIENT)
    public void updateBlock(World world) {
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
