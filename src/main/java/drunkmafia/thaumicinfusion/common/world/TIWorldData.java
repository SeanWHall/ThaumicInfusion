/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.util.Coordinate2List;
import drunkmafia.thaumicinfusion.common.util.helper.ReflectionHelper;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.Collections;

public class TIWorldData implements ISavable {

    public static Coordinate2List<TIWorldData> worldDatas = new Coordinate2List<TIWorldData>(TIWorldData.class);

    public World world;

    public Coordinate2List<ChunkData> chunkDatas = new Coordinate2List<ChunkData>(ChunkData.class);

    public static TIWorldData getWorldData(World world) {
        try {
            if (world == null || world.provider == null)
                return null;

            int dimensionID = world.provider.dimensionId;
            TIWorldData worldData = worldDatas.get(dimensionID, 0);

            if (worldData != null) {
                worldData.world = world;
                return worldData;
            }

            worldData = new TIWorldData();
            worldData.world = world;
            worldDatas.set(worldData, dimensionID, 0);
            return worldData;
        } catch (Exception e) {
        }
        return null;
    }

    public static World getWorld(IBlockAccess blockAccess) {
        return ThaumicInfusion.instance.side.isServer() ? blockAccess instanceof World ? (World) blockAccess : ReflectionHelper.getObjFromField(World.class, blockAccess) : ChannelHandler.getClientWorld();
    }

    /**
     * Adds block to world data
     *
     * @param block  to be saved to the world data
     * @param init   if true will initialize the data
     * @param packet will sync to the client if true
     */
    public void addBlock(BlockSavable block, boolean init, boolean packet){
        if (block == null)
            return;

        if (world == null)
            world = DimensionManager.getWorld(block.getCoords().dim);

        if (init && !block.isInit())
            block.dataLoad(world);

        WorldCoordinates coordinates = block.getCoords();

        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coordinates.x >> 4, coordinates.z >> 4);

        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition());
        if (chunkData == null)
            chunkData = chunkDatas.set(new ChunkData(chunkPos), chunkPos.getCenterXPos(), chunkPos.getCenterZPosition());

        chunkData.addBlock(block, coordinates.x, coordinates.y, coordinates.z);

        if (!world.isRemote && packet)
            ChannelHandler.network.sendToDimension(new BlockSyncPacketC(block), world.provider.dimensionId);
    }

    public ChunkData[] getChunksInRange(int xPos, int zPos, int xRange, int zRange) {
        ChunkData[] chunks = new ChunkData[xRange * zRange];
        int i = 0;
        for (int x = 0; x < xRange; x++) {
            for (int z = 0; z < zRange; z++) {
                chunks[i++] = chunkDatas.get(x + xPos, z + zPos);
            }
        }
        return chunks;
    }

    public void addBlock(BlockSavable block){
        addBlock(block, false, false);
    }

    public void postLoad(){
        for(BlockSavable savable : getAllStoredData()) {
            if (world == null)
                world = DimensionManager.getWorld(savable.getCoords().dim);
            else
                savable.getCoords().dim = world.provider.dimensionId;

            if (!savable.isInit())
                savable.dataLoad(world);
        }
    }

    public <T> T getBlock(Class<T> type, WorldCoordinates coords) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition());
        return chunkData != null ? chunkData.getBlock(type, coords.x, coords.y, coords.z) : null;
    }

    public void removeData(Class<?> type, WorldCoordinates coords, boolean sendPacket) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition());
        if(chunkData != null) {
            chunkData.removeBlock(coords.x, coords.y, coords.z);
            if (sendPacket) {
                coords.dim = world.provider.dimensionId;
                ChannelHandler.network.sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData() {
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        ChunkData[] chunks = chunkDatas.toArray();
        for (ChunkData chunk : chunks) {
            if (chunk != null)
                Collections.addAll(savables, chunk.getAllBlocks());
        }
        return savables.toArray(new BlockSavable[savables.size()]);
    }

    @Override
    public void readNBT(NBTTagCompound tag) {
        int size = tag.getInteger("Chunks");

        for(int i = 0; i < size; i++){
            if(!tag.hasKey("Chunk:" + i))
                continue;

            ChunkData chunkData = SavableHelper.loadDataFromNBT(tag.getCompoundTag("Chunk:" + i));
            if(chunkData != null){
                for (BlockSavable data : chunkData.getAllBlocks())
                    addBlock(data);
            }
        }
    }

    @Override
    public void writeNBT(NBTTagCompound tag) {
        ChunkData[] list = chunkDatas.toArray();
        tag.setInteger("Chunks", list.length);

        for (int i = 0; i < list.length; i++) {
            ChunkData chunkData = list[i];
            if (chunkData == null) continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}