/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import drunkmafia.thaumicinfusion.common.world.IWorldDataProvider;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.client.ChunkRequestPacketS;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CommonEventContainer {

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.world == null || event.world.isRemote || !(event.entity instanceof EntityPlayer))
            return;

        TIWorldData worldData = TIWorldData.getWorldData(event.world);
        if (worldData == null) return;
        for(BlockSavable savable : worldData.getAllStoredData()) {
            if (savable != null)
                ChannelHandler.instance().sendTo(new BlockSyncPacketC(savable), (EntityPlayerMP) event.entity);
        }
    }

    @SubscribeEvent
    public void loadChunk(ChunkEvent.Load event) {
        if (event.world == null) return;

        World world = event.world;
        if (world.isRemote)
            ChannelHandler.instance().sendToServer(new ChunkRequestPacketS(event.getChunk().getChunkCoordIntPair(), world.provider.dimensionId));
    }

    @SubscribeEvent
    public void unloadChunk(ChunkEvent.Unload event) {
        if (event.world == null) return;

        World world = event.world;
        if (!world.isRemote) return;

        TIWorldData worldData = TIWorldData.getWorldData(world);
        ChunkCoordIntPair pos = event.getChunk().getChunkCoordIntPair();
        worldData.chunkDatas.remove(pos.getCenterXPos(), pos.getCenterZPosition());
    }

    @SubscribeEvent
    public void load(WorldEvent.Load event) {
        if (event.world == null) return;

        World world = event.world;
        try {
            File file = new File("TIWorldData/" + world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData.dat");
            if (!file.exists())
                return;

            NBTTagCompound tagCompound = CompressedStreamTools.read(file);
            if (tagCompound == null)
                return;

            TIWorldData data = SavableHelper.loadDataFromNBT(tagCompound);

            if (data != null) {
                data.postLoad();
                data.world = world;
                ((IWorldDataProvider)world).setWorldData(data);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void save(WorldEvent.Save event) {
        if (event.world == null) return;

        World world = event.world;
        try {
            TIWorldData worldData = TIWorldData.getWorldData(world);
            NBTTagCompound tagCompound = SavableHelper.saveDataToNBT(worldData);
            if (tagCompound != null) {
                File file = new File("TIWorldData/" + world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData.dat");
                FileUtils.forceMkdir(new File("TIWorldData"));

                CompressedStreamTools.write(tagCompound, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
