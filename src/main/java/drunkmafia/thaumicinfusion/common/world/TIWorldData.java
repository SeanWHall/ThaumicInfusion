package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.util.helper.ReflectionHelper;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DrunkMafia on 18/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIWorldData extends WorldSavedData {

    public World world;

    private LongHashMap blocksData = new LongHashMap();
    private List<Long> chunkCoords = new ArrayList<Long>();

    public TIWorldData(String mapname) {
        super(mapname);
        setDirty(true);
    }

    public static TIWorldData getWorldData(World world) {
        if (world == null)
            return null;
        String worldName = world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData";
        WorldSavedData worldData = world.perWorldStorage.loadData(TIWorldData.class, worldName);
        if (worldData != null) {
            ((TIWorldData) worldData).world = world;
            return (TIWorldData) worldData;
        } else {
            worldData = new TIWorldData(worldName);
            ((TIWorldData) worldData).world = world;
            world.perWorldStorage.setData(worldName, worldData);
            return (TIWorldData) world.perWorldStorage.loadData(TIWorldData.class, worldName);
        }
    }

    public static <T extends BlockSavable> T getData(Class<T> type, World world, WorldCoord coords) {
        if (world == null)
            return null;

        coords.dim = world.provider.dimensionId;
        return getWorldData(world).getBlock(type, coords);
    }

    public static World getWorld(IBlockAccess blockAccess) {
        World world = ThaumicInfusion.instance.side.isServer() ? blockAccess instanceof World ? (World) blockAccess : ReflectionHelper.getObjFromField(World.class, blockAccess) : ChannelHandler.getClientWorld();
        return world;
    }

    public void addBlock(BlockSavable block, boolean init, boolean packet){
        try {
            if (block == null)
                return;

            if (world == null)
                world = DimensionManager.getWorld(block.getCoords().dim);

            if (init && !block.isInit())
                block.dataLoad(world);

            long coordHash = ChunkCoordIntPair.chunkXZ2Int(block.coordinates.x >> 4, block.coordinates.z >> 4);

            ChunkData chunkData = (ChunkData) blocksData.getValueByKey(coordHash);
            if (chunkData == null) {
                chunkData = new ChunkData(new ChunkCoordIntPair(block.coordinates.x >> 4, block.coordinates.z >> 4));
                blocksData.add(coordHash, chunkData);
                chunkCoords.add(coordHash);
            }

            chunkData.addBlock(block, block.coordinates.x, block.coordinates.y, block.coordinates.z);

            setDirty(true);
            CooldownPacket.syncTimeouts.remove(block.getCoords());
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
        ChunkData chunkData = (ChunkData) blocksData.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(coords.x >> 4, coords.z >> 4));
        return chunkData != null ? chunkData.getBlock(type, coords.x, coords.y, coords.z) : null;
    }

    public void removeData(Class<?> type, WorldCoord coords, boolean sendPacket) {
        ChunkData chunkData = (ChunkData) blocksData.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(coords.x >> 4, coords.z >> 4));
        if(chunkData != null) {
            setDirty(true);
            chunkData.removeBlock(coords.x, coords.y, coords.z);
            if (sendPacket) {
                coords.dim = world.provider.dimensionId;
                ChannelHandler.network.sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData(){
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        for(long coord : chunkCoords){
            ChunkData data = (ChunkData) blocksData.getValueByKey(coord);
            if(data != null) {
                for(BlockSavable block : data.getAllBlocks())
                    savables.add(block);
            }
        }
        return savables.toArray(new BlockSavable[savables.size()]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
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
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("Chunks", chunkCoords.size());
        for(int i = 0; i < chunkCoords.size(); i++){
            ChunkData chunkData = (ChunkData) blocksData.getValueByKey(chunkCoords.get(i));
            if(chunkData == null)
                continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}