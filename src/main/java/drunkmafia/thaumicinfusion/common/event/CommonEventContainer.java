package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;
import java.io.IOException;

/**
 * Created by DrunkMafia on 04/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class CommonEventContainer {

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {
        BlockSavable[] datas = TIWorldData.getWorldData(event.entityPlayer.worldObj).getAllStoredData();
        for (BlockSavable savable : datas) {
            if (savable instanceof BlockData) {
                BlockData data = (BlockData) savable;
                for (AspectEffect effect : data.getEffects())
                    effect.interactWithEntity(event.entityPlayer, event.target);
            }
        }
    }

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
    public void load(WorldEvent.Load event) throws IOException {
        World world = event.world;
        NBTTagCompound tagCompound = CompressedStreamTools.read(new File("world/data/" + world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData"));
        if (tagCompound == null)
            return;

        TIWorldData data = SavableHelper.loadDataFromNBT(tagCompound);

        if (data != null) {
            data.postLoad();
            TIWorldData.worldDatas.set(data, world.provider.dimensionId, 0);
        }
    }

    @SubscribeEvent
    public void save(WorldEvent.Save event) throws IOException {
        World world = event.world;
        TIWorldData worldData = TIWorldData.getWorldData(world);
        NBTTagCompound tagCompound = SavableHelper.saveDataToNBT(worldData);
        if (tagCompound != null)
            CompressedStreamTools.write(tagCompound, new File("world/data/" + world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData"));
    }
}
