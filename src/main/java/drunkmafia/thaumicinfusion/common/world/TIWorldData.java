package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.lib.ConfigHandler;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import drunkmafia.thaumicinfusion.net.packet.client.RequestBlockPacketS;
import drunkmafia.thaumicinfusion.net.packet.server.BlockDestroyedPacketC;
import gnu.trove.map.hash.THashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by DrunkMafia on 18/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIWorldData extends WorldSavedData {

    public World world;
    private THashMap<WorldCoord, ArrayList<BlockSavable>> blocksData;

    public TIWorldData(String mapname) {
        super(mapname);
        blocksData = new THashMap<WorldCoord, ArrayList<BlockSavable>>();
        setDirty(true);
    }

    public void addBlock(BlockSavable block, boolean init){
        if(block == null)
            return;

        if(world == null)
            world = DimensionManager.getWorld(block.getCoords().dim);

        cleanDataAt(block);

        if(init && !block.isInit())
            block.dataLoad(world);

        if(blocksData.containsKey(block.getCoords()) && !blocksData.get(block.getCoords()).contains(block)) {
            blocksData.get(block.getCoords()).add(block);
        }else {
            ArrayList<BlockSavable> datas = new ArrayList<BlockSavable>();
            datas.add(block);
            blocksData.put(block.getCoords(), datas);
        }
        setDirty(true);
        CooldownPacket.syncTimeouts.remove(block.getCoords());
    }

    public void addBlock(BlockSavable block){
        addBlock(block, false);
    }

    public void postLoad(){
        for(BlockSavable[] blocks : getAllStoredData()) {
            for(BlockSavable savable : blocks) {
                if (world == null)
                    world = DimensionManager.getWorld(savable.getCoords().dim);
                else
                    savable.coordinates.dim = world.provider.dimensionId;

                if (!savable.isInit())
                    savable.dataLoad(world);
                cleanDataAt(savable);
            }
        }
    }

    public void removeBlock(WorldCoord coords){
        removeBlock(coords, false);
    }

    public void removeBlock(WorldCoord coords, boolean sendPacket) {
        if(coords == null || !blocksData.containsKey(coords))
            return;

        blocksData.remove(coords);

        setDirty(true);

        world.setBlock(coords.x, coords.y, coords.z, Blocks.air);
        world.removeTileEntity(coords.x, coords.y, coords.z);

        if(sendPacket && !world.isRemote)
            ChannelHandler.network.sendToDimension(new BlockDestroyedPacketC(coords), world.provider.dimensionId);
    }

    public void cleanDataAt(BlockSavable savable){
        if(blocksData == null || world == null)
            return;

        WorldCoord pos = savable.getCoords();
        Block block = world.getBlock(pos.x, pos.y, pos.z);

        if(block == null) {
            removeBlock(pos, true);
            return;
        }

        if(savable.getBlock() != null && block != savable.getBlock() && blocksData.containsKey(savable.getCoords())) {
            blocksData.get(pos).remove(savable);
            return;
        }
    }

    long start;

    public void cleanAllData(){
        if(world.isRemote)
            return;
        if(System.currentTimeMillis() > start + ConfigHandler.maxTimeout){
            BlockSavable[][] savables = getAllStoredData();
            for(BlockSavable[] pos : savables) {
                if (pos.length > 0)
                    cleanDataAt(pos[0]);
            }
            start = System.currentTimeMillis();
        }
    }

    public BlockSavable[] getAllDatasAt(WorldCoord coords){
        ArrayList<BlockSavable> savables = blocksData.get(coords);
        if(savables != null)
            return blocksData.get(coords).toArray(new BlockSavable[savables.size()]);
        return new BlockSavable[0];
    }

    public <T>T getBlock(Class<T> type, WorldCoord coords) {
        ArrayList<BlockSavable> datas = blocksData.get(coords);
        if(datas == null)
            return null;

        for(BlockSavable block : datas) {
            if (type.isAssignableFrom(block.getClass())) {
                cleanDataAt(block);
                return (T) block;
            }
        }
        return null;
    }

    public BlockSavable[][] getAllStoredData(){
        Map.Entry<WorldCoord, ArrayList<BlockSavable>>[] entries = blocksData.entrySet().toArray(new Map.Entry[blocksData.size()]);
        BlockSavable[][] savables = new BlockSavable[entries.length][0];
        for(int i = 0; i < savables.length; i++){
            ArrayList<BlockSavable> stored = entries[i].getValue();
            savables[i] = new BlockSavable[stored.size()];
            for(int s = 0; s < stored.size(); s++)
                savables[i][s] = stored.get(s);
        }
        return savables;
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockSavable>T[] getAllBlocks(Class<T> type){
        BlockSavable[][] blocks = getAllStoredData();
        ArrayList<T> blocksOfType = new ArrayList<T>();
        for(BlockSavable[] blocksAtPos : blocks){
            for(BlockSavable block : blocksAtPos) {
                if (type.isInstance(block))
                    blocksOfType.add(type.cast(block));
            }
        }
        return blocksOfType.toArray((T[]) Array.newInstance(type, blocksOfType.size()));
    }

    public int size(){
        return blocksData.size();
    }

    public static TIWorldData getWorldData(World world) {
        String worldName = world.getWorldInfo().getWorldName() + "_" + world.provider.dimensionId + "_TIWorldData";
        WorldSavedData worldData = world.perWorldStorage.loadData(TIWorldData.class, worldName);
        if (worldData != null) {
            ((TIWorldData) worldData).world = world;
            return (TIWorldData) worldData;
        }else {
            worldData = new TIWorldData(worldName);
            ((TIWorldData) worldData).world = world;
            world.perWorldStorage.setData(worldName, worldData);
            return (TIWorldData) world.perWorldStorage.loadData(TIWorldData.class, worldName);
        }
    }

    public static <T>T getData(Class<T> type, World world, WorldCoord coords) {
        if(world == null)
            return null;
        coords.dim = world.provider.dimensionId;

        T data = getWorldData(world).getBlock(type, coords);

        if (data == null && world.isRemote) {
            coords.dim = Minecraft.getMinecraft().theWorld.provider.dimensionId;
            ChannelHandler.network.sendToServer(new RequestBlockPacketS((Class<? extends BlockSavable>) type, coords));
        }
        return data;
    }

    public static World getWorld(IBlockAccess blockAccess) {
        if(ThaumicInfusion.instance.isServer)
            return blockAccess instanceof World ? (World) blockAccess : ((ChunkCache)blockAccess).worldObj;
        else
            return ChannelHandler.getClientWorld();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        for(int i = 0; i < tag.getInteger("Positions"); i++){
            for(int p = 0; p < tag.getInteger("Pos: " + i); p++){
                NBTTagCompound dataTag = tag.getCompoundTag("Pos: " + i + " Tag: " + p);
                BlockSavable data = Savable.loadDataFromNBT(dataTag);

                if(blocksData.containsKey(data.getCoords())) {
                    blocksData.get(data.getCoords()).add(data);
                }else {
                    ArrayList<BlockSavable> datas = new ArrayList<BlockSavable>();
                    datas.add(data);
                    blocksData.put(data.getCoords(), datas);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        BlockSavable[][] storedData = getAllStoredData();
        tag.setInteger("Positions", storedData.length);
        for(int i = 0; i < storedData.length; i++){
            tag.setInteger("Pos: " + i, storedData[i].length);
            for(int p = 0; p < storedData[i].length; p++){
                NBTTagCompound dataTag = new NBTTagCompound();
                storedData[i][p].writeNBT(dataTag);
                tag.setTag("Pos: " + i + " Tag: " + p, dataTag);
            }
        }
    }
}