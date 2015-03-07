package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.util.helper.ReflectionHelper;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.CooldownPacket;
import drunkmafia.thaumicinfusion.net.packet.server.BlockDestroyedPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrunkMafia on 18/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIWorldData extends WorldSavedData {

    public World world;
    private Map<WorldCoord, ArrayList<BlockSavable>> blocksData;

    public TIWorldData(String mapname) {
        super(mapname);
        blocksData = new HashMap<WorldCoord, ArrayList<BlockSavable>>();
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

    public void addBlock(BlockSavable block, boolean init){
        if(block == null)
            return;

        if(world == null)
            world = DimensionManager.getWorld(block.getCoords().dim);

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
        if (!world.isRemote)
            ChannelHandler.network.sendToAll(new BlockSyncPacketC(block));
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

        if(sendPacket && !world.isRemote)
            ChannelHandler.network.sendToDimension(new BlockDestroyedPacketC(coords), world.provider.dimensionId);
    }

    public ArrayList<BlockSavable> getAllDatasAt(WorldCoord coords) {
        ArrayList<BlockSavable> savables = blocksData.get(coords);
        if(savables != null)
            return savables;
        return new ArrayList<BlockSavable>();
    }

    public <T>T getBlock(Class<T> type, WorldCoord coords) {
        ArrayList<BlockSavable> datas = blocksData.get(coords);
        if(datas != null) {
            for (BlockSavable block : datas) {
                if (type.isAssignableFrom(block.getClass())) {
                    return type.cast(block);
                }
            }
        }
        return null;
    }

    public void removeData(Class<?> type, WorldCoord coords, boolean sendPacket) {
        ArrayList<BlockSavable> savables = blocksData.get(coords);
        if (savables == null)
            return;

        for (int i = 0; i < savables.size(); i++) {
            if (type.isAssignableFrom(savables.get(i).getClass())) {
                savables.remove(i);
                if (sendPacket) {
                    coords.dim = world.provider.dimensionId;
                    System.out.println(DimensionManager.getWorld(world.provider.dimensionId) != null);
                    ChannelHandler.network.sendToAll(new DataRemovePacketC(type, coords));
                }
            }
        }
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

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        for(int i = 0; i < tag.getInteger("Positions"); i++){
            for(int p = 0; p < tag.getInteger("Pos: " + i); p++){
                BlockSavable data = SavableHelper.loadDataFromNBT(tag.getCompoundTag("Pos: " + i + " Tag: " + p));

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
            for(int p = 0; p < storedData[i].length; p++)
                tag.setTag("Pos: " + i + " Tag: " + p, SavableHelper.saveDataToNBT(storedData[i][p]));
        }
    }
}