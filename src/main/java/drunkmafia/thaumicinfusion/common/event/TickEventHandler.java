package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TickEventHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void clientTick(TickEvent.ClientTickEvent event){
        World world = Minecraft.getMinecraft().theWorld;
        if (world != null) {
            tickWorld(world);
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event){
        tickWorld(event.world);
    }

    void tickWorld(World world){
        BlockData[] datas = TIWorldData.getWorldData(world).getAllBlocks(BlockData.class);
        for(BlockData data : datas) {
            data.tickData();
            for (AspectEffect effect : data.getEffects()) {
                effect.updateBlock(world);
            }
        }
    }
}
