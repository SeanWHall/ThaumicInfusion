package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Created by DrunkMafia on 04/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class CommonEventContainer {

    @SubscribeEvent
    public void onClick(PlayerInteractEvent event) {
        BlockSavable[] datas = TIWorldData.getWorldData(event.world).getAllStoredData();
        for(BlockSavable savable : datas) {
            if(savable instanceof BlockData) {
                BlockData data = (BlockData) savable;
                for (AspectEffect effect : data.getEffects()) {
                    effect.worldBlockInteracted(event.entityPlayer, event.world, event.x, event.y, event.z, event.face);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.world.isRemote || !(event.entity instanceof EntityPlayer))
            return;

        TIWorldData worldData = TIWorldData.getWorldData(event.world);
        for(BlockSavable savable : worldData.getAllStoredData()) {
            if (savable instanceof BlockData)
                ChannelHandler.network.sendTo(new BlockSyncPacketC(savable), (EntityPlayerMP) event.entity);
        }

    }

    @SubscribeEvent
    public void load(WorldEvent.Load event){
        TIWorldData data = TIWorldData.getWorldData(event.world);
        if(data != null)
            data.postLoad();
    }
}
