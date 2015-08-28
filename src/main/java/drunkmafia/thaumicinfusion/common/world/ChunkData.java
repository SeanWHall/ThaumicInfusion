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

    public boolean addBlock(BlockSavable data, int x, int y, int z) {
        return !(y < 0 || y > 256) && (blockdata[x & 15][y][z & 15] != null ? blockdata[x & 15][y][z & 15] : (blockdata[x & 15][y][z & 15] = new ArrayList<BlockSavable>())).add(data);
    }

    public void removeBlock(int x, int y, int z){
        blockdata[x & 15][y][z & 15] = null;
    }

    public boolean removeData(Class<? extends BlockSavable> type, int x, int y, int z){
        if(y < 0 || y > 256) return false;

        List<BlockSavable> datas = blockdata[x & 15][y][z & 15];
        if(datas == null)
            return false;

        for (int i = 0; i < datas.size(); i++) {
            BlockSavable block = datas.get(i);
            if (type.isAssignableFrom(block.getClass()))
                datas.remove(block);
        }

        if(datas.size() == 0)
            blockdata[x & 15][y][z & 15] = null;
        return true;
    }

    public <T>T getBlock(Class<T> type, int x, int y, int z){
        if (y < 0 || y >= 256) return null;

        if(blockdata[x & 15][y][z & 15] != null) {
            for (BlockSavable block : blockdata[x & 15][y][z & 15]) {
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
                    if(blockdata[x][y][z] != null){
                        tagCompound.setInteger("SIZE X:" + x + "Y:" + y + "Z:" + z, blockdata[x][y][z].size());
                        for(int i = 0; i < blockdata[x][y][z].size(); i++)
                            tagCompound.setTag("Chunk:" + ChunkCoordIntPair.chunkXZ2Int(chunkPos.chunkXPos, chunkPos.chunkZPos) + "X:" + x + "Y:" + y + "Z:" + z + "ID:" + i, SavableHelper.saveDataToNBT(blockdata[x][y][z].get(i)));
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
