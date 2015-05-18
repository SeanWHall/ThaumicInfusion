package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.util.Coordinate2List;
import drunkmafia.thaumicinfusion.common.util.helper.ReflectionHelper;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DrunkMafia on 18/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIWorldData implements ISavable {

    public static Coordinate2List<TIWorldData> worldDatas = new Coordinate2List<TIWorldData>(TIWorldData.class);

    public World world;

    private Coordinate2List<ChunkData> blocksData = new Coordinate2List<ChunkData>(ChunkData.class);

    public static TIWorldData getWorldData(World world) {
        if (world == null || world.getWorldInfo() == null || world.provider == null)
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
    }

    public static <T extends BlockSavable> T getData(Class<T> type, World world, WorldCoord coords) {
        if (world == null)
            return null;

        coords.dim = world.provider.dimensionId;
        return getWorldData(world).getBlock(type, coords);
    }

    public static World getWorld(IBlockAccess blockAccess) {
        return ThaumicInfusion.instance.side.isServer() ? blockAccess instanceof World ? (World) blockAccess : ReflectionHelper.getObjFromField(World.class, blockAccess) : ChannelHandler.getClientWorld();
    }

    public void addBlock(BlockSavable block, boolean init, boolean packet){
        try {
            if (block == null)
                return;

            if (world == null)
                world = DimensionManager.getWorld(block.getCoords().dim);

            if (init && !block.isInit())
                block.dataLoad(world);

            ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(block.coordinates.x >> 4, block.coordinates.z >> 4);

            ChunkData chunkData = blocksData.get(chunkPos.chunkXPos, chunkPos.chunkZPos);
            if (chunkData == null)
                chunkData = blocksData.set(new ChunkData(chunkPos), chunkPos.chunkXPos, chunkPos.chunkZPos);

            chunkData.addBlock(block, block.coordinates.x, block.coordinates.y, block.coordinates.z);

            if (!world.isRemote && packet)
                ChannelHandler.network.sendToAll(new BlockSyncPacketC(block));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addBlock(BlockSavable block){
        addBlock(block, false, false);
    }

    public void postLoad(){
        for(BlockSavable savable : getAllStoredData()) {
            if (world == null)
                world = DimensionManager.getWorld(savable.getCoords().dim);
            else
                savable.coordinates.dim = world.provider.dimensionId;

            if (!savable.isInit())
                savable.dataLoad(world);
        }
    }

    public <T>T getBlock(Class<T> type, WorldCoord coords) {
        ChunkData chunkData = blocksData.get(coords.x >> 4, coords.z >> 4);
        return chunkData != null ? chunkData.getBlock(type, coords.x, coords.y, coords.z) : null;
    }

    public void removeData(Class<?> type, WorldCoord coords, boolean sendPacket) {
        ChunkData chunkData = blocksData.get(coords.x >> 4, coords.z >> 4);
        if(chunkData != null) {
            chunkData.removeBlock(coords.x, coords.y, coords.z);
            if (sendPacket) {
                coords.dim = world.provider.dimensionId;
                ChannelHandler.network.sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData(){
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        for (ChunkData data : blocksData.toList()) {
            if (data != null)
                Collections.addAll(savables, data.getAllBlocks());
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
        List<ChunkData> list = blocksData.toList();
        tag.setInteger("Chunks", list.size());
        for (int i = 0; i < list.size(); i++) {
            ChunkData chunkData = list.get(i);
            if (chunkData == null) continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}