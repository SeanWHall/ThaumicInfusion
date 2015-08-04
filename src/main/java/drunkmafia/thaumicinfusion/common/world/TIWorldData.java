/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.util.helper.ReflectionLookup;
import drunkmafia.thaumicinfusion.common.util.quadtree.QuadTree;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.*;

public class TIWorldData implements ISavable {

    private static ReflectionLookup<World> worldLookup = new ReflectionLookup<World>(World.class);

    public World world;
    public QuadTree<ChunkData> chunkDatas = new QuadTree<ChunkData>(ChunkData.class, -2000000, -2000000, 2000000, 2000000);

    @Nullable
    public static TIWorldData getWorldData(World world) {
        if (world == null || !(world instanceof IWorldDataProvider))
            return null;

        IWorldDataProvider dataProvider = (IWorldDataProvider) world;
        TIWorldData worldData = dataProvider.getWorldData();

        if(!world.isRemote) world = DimensionManager.getWorld(world.provider.dimensionId);
        if (worldData == null) dataProvider.setWorldData(worldData = new TIWorldData());

        worldData.world = world;
        return worldData;
    }

    public static World getWorld(IBlockAccess blockAccess) {
        return blockAccess != null ? blockAccess instanceof World ? (World) blockAccess : worldLookup.getObjectFrom(blockAccess) : null;
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
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if (chunkData == null) {
            chunkData = new ChunkData(chunkPos);
            chunkDatas.set(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), chunkData);
        }
        chunkData.addBlock(block, coordinates.x, coordinates.y, coordinates.z);

        if (!world.isRemote && packet)
            ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(block), world.provider.dimensionId);
    }

    public ChunkData[] getChunksInRange(int xPos, int zPos, int xRange, int zRange) {
        ChunkData[] chunks = new ChunkData[xRange * zRange];
        int i = 0;
        for (int x = 0; x < xRange; x++) {
            for (int z = 0; z < zRange; z++) {
                chunks[i++] = chunkDatas.get(x + xPos, z + zPos, null);
            }
        }
        return chunks;
    }

    public void addBlock(BlockSavable block){
        addBlock(block, false, false);
    }

    public void postLoad(){
        for(BlockSavable savable : getAllStoredData()) {
            if(savable == null) continue;

            if (world == null) world = DimensionManager.getWorld(savable.getCoords().dim);
            else savable.getCoords().dim = world.provider.dimensionId;

            if (!savable.isInit())  savable.dataLoad(world);
        }
    }

    public <T> T getBlock(Class<T> type, WorldCoordinates coords) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        return chunkData != null ? chunkData.getBlock(type, coords.x, coords.y, coords.z) : null;
    }

    public void removeData(Class<? extends BlockSavable> type, WorldCoordinates coords, boolean sendPacket) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if(chunkData != null) {
            chunkData.removeData(type, coords.x, coords.y, coords.z);
            if (sendPacket) {
                coords.dim = world.provider.dimensionId;
                ChannelHandler.instance().sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData() {
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        for(ChunkData chunks : chunkDatas.getValues())
            Collections.addAll(savables, chunks.getAllBlocks());
        return savables.toArray(new BlockSavable[1]);
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
        ChunkData[] chunks = chunkDatas.getValues();
        tag.setInteger("Chunks", chunks.length);

        for (int i = 0; i < chunks.length; i++) {
            ChunkData chunkData = chunks[i];
            if (chunkData == null) continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}