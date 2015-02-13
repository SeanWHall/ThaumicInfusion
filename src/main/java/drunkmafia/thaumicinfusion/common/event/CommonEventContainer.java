package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
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
                try {
                    effect.worldBlockInteracted(event.entityPlayer, event.world, event.x, event.y, event.z, event.face);
                } catch (Exception e) {
                    InfusedBlock.handleError(e, event.world, effect.data, true);
                }
            }
        }
    }

    @SubscribeEvent
    public void load(WorldEvent.Load event){
        TIWorldData data = TIWorldData.getWorldData(event.world);
        data.postLoad();
    }
}
