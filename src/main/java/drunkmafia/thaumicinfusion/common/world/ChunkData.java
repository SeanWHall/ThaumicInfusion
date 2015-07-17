/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.ArrayList;
import java.util.List;

public class ChunkData implements ISavable{

    protected ChunkCoordIntPair chunkPos;
    private List<BlockSavable>[][][] blockdata = new ArrayList[16][256][16];

    public ChunkData(){}

    public ChunkData(ChunkCoordIntPair chunkPos) {
        this.chunkPos = chunkPos;
    }

    public BlockSavable[] getAllBlocks(){
        ArrayList<BlockSavable> allData = new ArrayList<BlockSavable>();
        for(int x = 0; x < blockdata.length; x++) {
            for (int y = 0; y < blockdata[x].length; y++) {
                for (int z = 0; z < blockdata[x][y].length; z++) {
                    final List<BlockSavable> savables = blockdata[x][y][z];
                    if(savables == null) continue;
                    for (BlockSavable data : savables)
                        allData.add(data);
                }
            }
        }

        return allData.toArray(new BlockSavable[allData.size()]);
    }

    public void addBlock(BlockSavable data, int x, int y, int z){
        if(y < 0 || y > 256) return;
        (blockdata[x & 15][y][z & 15] != null ? blockdata[x & 15][y][z & 15] : (blockdata[x & 15][y][z & 15] = new ArrayList<BlockSavable>())).add(data);
    }

    public void removeBlock(int x, int y, int z){
        blockdata[x & 15][y][z & 15] = null;
    }

    public void removeData(Class<? extends BlockSavable> type, int x, int y, int z){
        if(y < 0 || y > 256) return;

        List<BlockSavable> datas = blockdata[x & 15][y][z & 15];
        if(datas == null)
            return;

        for (int i = 0; i < datas.size(); i++) {
            BlockSavable block = datas.get(i);
            if (type.isAssignableFrom(block.getClass()))
                datas.remove(block);
        }

        if(datas.size() == 0)
            blockdata[x & 15][y][z & 15] = null;
    }

    public <T>T getBlock(Class<T> type, int x, int y, int z){
        if(y < 0 || y > 256) return null;

        List<BlockSavable> datas = blockdata[x & 15][y][z & 15];
        if(datas != null) {
            for (BlockSavable block : datas) {
                if (type.isAssignableFrom(block.getClass())) {
                    return type.cast(block);
                }
            }
        }
        return null;
    }

    public ChunkCoordIntPair getChunkPos() {
        return chunkPos;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("ChunkX", chunkPos.chunkXPos);
        tagCompound.setInteger("ChunkZ", chunkPos.chunkZPos);

        for(int x = 0; x < blockdata.length; x++){
            for(int y = 0; y < blockdata[x].length; y++){
                for(int z = 0; z < blockdata[x][y].length; z++){
                    List<BlockSavable> datas = blockdata[x][y][z];
                    if(datas != null){
                        tagCompound.setInteger("SIZE X:" + x + "Y:" + y + "Z:" + z, datas.size());
                        for(int i = 0; i < datas.size(); i++)
                            tagCompound.setTag("Chunk:" + ChunkCoordIntPair.chunkXZ2Int(chunkPos.chunkXPos, chunkPos.chunkZPos) + "X:" + x + "Y:" + y + "Z:" + z + "ID:" + i, SavableHelper.saveDataToNBT(datas.get(i)));
                    }
                }
            }
        }
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        chunkPos = new ChunkCoordIntPair(tagCompound.getInteger("ChunkX"), tagCompound.getInteger("ChunkZ"));

        for(int x = 0; x < blockdata.length; x++) {
            for (int y = 0; y < blockdata[x].length; y++) {
                for (int z = 0; z < blockdata[x][y].length; z++) {
                    if(tagCompound.hasKey("SIZE X:" + x + "Y:" + y + "Z:" + z)){
                        int Size = tagCompound.getInteger("SIZE X:" + x + "Y:" + y + "Z:" + z);
                        List<BlockSavable> datas = new ArrayList<BlockSavable>();
                        for(int i = 0; i < Size; i++)
                             datas.add(SavableHelper.<BlockSavable>loadDataFromNBT(tagCompound.getCompoundTag("Chunk:" + ChunkCoordIntPair.chunkXZ2Int(chunkPos.chunkXPos, chunkPos.chunkZPos) + "X:" + x + "Y:" + y + "Z:" + z + "ID:" + i)));
                        blockdata[x][y][z] = datas;
                    }
                }
            }
        }
    }
}
