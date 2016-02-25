/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.util.helper.ReflectionLookup;
import drunkmafia.thaumicinfusion.common.util.helper.SavableHelper;
import drunkmafia.thaumicinfusion.common.util.quadtree.QuadTree;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TIWorldData implements ISavable {

    private static ReflectionLookup<World> worldLookup = new ReflectionLookup<World>(World.class);

    public World world;

    //The whole storage system for data, has been designed for extremely fast single coordinate queries and not looping
    public QuadTree<ChunkData> chunkDatas = new QuadTree<ChunkData>(ChunkData.class, -2000000, -2000000, 2000000, 2000000);

    public static TIWorldData getWorldData(World world) {
        if (world == null || !(world instanceof IWorldDataProvider))
            return null;

        IWorldDataProvider dataProvider = (IWorldDataProvider) world;
        TIWorldData worldData = dataProvider.getWorldData();

        if (worldData == null) dataProvider.setWorldData(worldData = new TIWorldData());

        worldData.world = world;
        return worldData;
    }

    public static World getWorld(IBlockAccess blockAccess) {
        if (TIWorldData.worldLookup == null) TIWorldData.worldLookup = new ReflectionLookup<World>(World.class);
        return blockAccess != null ? blockAccess instanceof World ? (World) blockAccess : TIWorldData.worldLookup.getObjectFrom(blockAccess) : null;
    }

    /**
     * Adds block to world data
     *
     * @param block  to be saved to the world data
     * @param init   if true will initialize the data
     * @param packet will sync to the client if true
     */
    public void addBlock(BlockSavable block, boolean init, boolean packet) {
        if (block == null)
            return;

        if (this.world == null)
            this.world = DimensionManager.getWorld(block.getCoords().dim);

        BlockPos coordinates = block.getCoords().pos;

        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coordinates.getX() >> 4, coordinates.getZ() >> 4);
        ChunkData chunkData = this.chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if (chunkData == null) {
            chunkData = new ChunkData(chunkPos);
            this.chunkDatas.set(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), chunkData);
        }
        chunkData.addBlock(block, coordinates);

        if (init && !block.isInit())
            block.dataLoad(this.world);

        if (!this.world.isRemote && packet)
            ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(block), this.world.provider.getDimensionId());
    }

    public List<ChunkData> getChunksInRange(int xMin, int zMin, int xMax, int zMax) {
        return this.chunkDatas.searchWithinObject(xMin, zMin, xMax, zMax);
    }

    public void addBlock(BlockSavable block) {
        this.addBlock(block, false, false);
    }

    public void postLoad() {
        for (BlockSavable savable : this.getAllStoredData()) {
            if (savable == null) continue;

            if (this.world == null) this.world = DimensionManager.getWorld(savable.getCoords().dim);
            else savable.getCoords().dim = this.world.provider.getDimensionId();

            if (!savable.isInit()) savable.dataLoad(this.world);
        }
    }

    /**
     * Grabs a BlockSavable from the Quadtree at a specified location and casted to a certain type.
     *
     * @param type   The class of the data you want to get
     * @param coords The position that you want to grab data from
     * @param <T>    The Type of Data that will be returned
     */
    public <T> T getBlock(Class<T> type, WorldCoordinates coords) {
        if (coords == null || coords.pos == null || type == null) return null;
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.pos.getX() >> 4, coords.pos.getZ() >> 4);
        ChunkData chunkData = this.chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        return chunkData != null ? chunkData.getBlock(type, coords.pos) : null;
    }

    public <T> T getBlock(Class<T> type, BlockPos pos) {
        return getBlock(type, new WorldCoordinates(pos, world.provider.getDimensionId()));
    }

    /**
     * Will remove a specific Data from a position in the world.
     *
     * @param type       The type you want to remove
     * @param coords     The coordinates of the data
     * @param sendPacket Whether or not the change should be sent to clients
     */
    public void removeData(Class<? extends BlockSavable> type, WorldCoordinates coords, boolean sendPacket) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.pos.getX() >> 4, coords.pos.getZ() >> 4);
        ChunkData chunkData = this.chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if (chunkData != null) {
            chunkData.removeData(type, coords.pos);
            if (sendPacket) {
                coords.dim = this.world.provider.getDimensionId();
                ChannelHandler.instance().sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData() {
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        for (ChunkData chunks : this.chunkDatas.getValues())
            Collections.addAll(savables, chunks.getAllBlocks());
        return savables.size() != 0 ? savables.toArray(new BlockSavable[1]) : new BlockSavable[0];
    }

    @Override
    public void readNBT(NBTTagCompound tag) {
        int size = tag.getInteger("Chunks");

        for (int i = 0; i < size; i++) {
            if (!tag.hasKey("Chunk:" + i))
                continue;

            ChunkData chunkData = SavableHelper.loadDataFromNBT(tag.getCompoundTag("Chunk:" + i));
            if (chunkData != null) {
                for (BlockSavable data : chunkData.getAllBlocks())
                    this.addBlock(data);
            }
        }
    }

    @Override
    public void writeNBT(NBTTagCompound tag) {
        ChunkData[] chunks = this.chunkDatas.getValues();
        tag.setInteger("Chunks", chunks.length);

        for (int i = 0; i < chunks.length; i++) {
            ChunkData chunkData = chunks[i];
            if (chunkData == null) continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}
