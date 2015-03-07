package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
        BlockData[] datas = TIWorldData.getWorldData(event.world).getAllBlocks(BlockData.class);
        for(BlockData data : datas) {
            for (AspectEffect effect : data.getEffects()) {
                effect.worldBlockInteracted(event.entityPlayer, event.world, event.x, event.y, event.z, event.face);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.world.isRemote || !(event.entity instanceof EntityPlayer))
            return;

        TIWorldData data = TIWorldData.getWorldData(event.world);
        for (BlockSavable block : data.getAllBlocks(BlockSavable.class))
            ChannelHandler.network.sendTo(new BlockSyncPacketC(block), (EntityPlayerMP) event.entity);

    }

    @SubscribeEvent
    public void load(WorldEvent.Load event){
        TIWorldData data = TIWorldData.getWorldData(event.world);

        data.postLoad();
    }
}
