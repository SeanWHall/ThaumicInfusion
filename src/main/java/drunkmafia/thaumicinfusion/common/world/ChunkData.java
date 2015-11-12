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

public class ChunkData implements ISavable {

    private final List<BlockSavable>[][][] blockdata = new ArrayList[16][256][16];
    public int instability;
    protected ChunkCoordIntPair chunkPos;

    public ChunkData() {
    }

    public ChunkData(ChunkCoordIntPair chunkPos) {
        this.chunkPos = chunkPos;
    }

    public BlockSavable[] getAllBlocks() {
        ArrayList<BlockSavable> allData = new ArrayList<BlockSavable>();
        for (int x = 0; x < this.blockdata.length; x++) {
            for (int y = 0; y < this.blockdata[x].length; y++) {
                for (int z = 0; z < this.blockdata[x][y].length; z++) {
                    List<BlockSavable> savables = this.blockdata[x][y][z];
                    if (savables == null) continue;
                    for (BlockSavable data : savables)
                        allData.add(data);
                }
            }
        }

        return allData.toArray(new BlockSavable[allData.size()]);
    }

    public boolean addBlock(BlockSavable data, int x, int y, int z) {
        return !(y < 0 || y > 256) && (this.blockdata[x & 15][y][z & 15] != null ? this.blockdata[x & 15][y][z & 15] : (this.blockdata[x & 15][y][z & 15] = new ArrayList<BlockSavable>())).add(data);
    }

    public void removeBlock(int x, int y, int z) {
        this.blockdata[x & 15][y][z & 15] = null;
    }

    public boolean removeData(Class<? extends BlockSavable> type, int x, int y, int z) {
        if (y < 0 || y > 256) return false;

        List<BlockSavable> datas = this.blockdata[x & 15][y][z & 15];
        if (datas == null)
            return false;

        for (int i = 0; i < datas.size(); i++) {
            BlockSavable block = datas.get(i);
            if (type.isAssignableFrom(block.getClass()))
                datas.remove(block);
        }

        if (datas.size() == 0)
            this.blockdata[x & 15][y][z & 15] = null;
        return true;
    }

    public <T> T getBlock(Class<T> type, int x, int y, int z) {
        if (y < 0 || y >= 256) return null;

        if (this.blockdata[x & 15][y][z & 15] != null) {
            for (BlockSavable block : this.blockdata[x & 15][y][z & 15]) {
                if (type.isAssignableFrom(block.getClass())) {
                    return type.cast(block);
                }
            }
        }
        return null;
    }

    public ChunkCoordIntPair getChunkPos() {
        return this.chunkPos;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("ChunkX", this.chunkPos.chunkXPos);
        tagCompound.setInteger("ChunkZ", this.chunkPos.chunkZPos);

        tagCompound.setInteger("instability", this.instability);

        for (int x = 0; x < this.blockdata.length; x++) {
            for (int y = 0; y < this.blockdata[x].length; y++) {
                for (int z = 0; z < this.blockdata[x][y].length; z++) {
                    if (this.blockdata[x][y][z] != null) {
                        tagCompound.setInteger("SIZE X:" + x + "Y:" + y + "Z:" + z, this.blockdata[x][y][z].size());
                        for (int i = 0; i < this.blockdata[x][y][z].size(); i++)
                            tagCompound.setTag("Chunk:" + ChunkCoordIntPair.chunkXZ2Int(this.chunkPos.chunkXPos, this.chunkPos.chunkZPos) + "X:" + x + "Y:" + y + "Z:" + z + "ID:" + i, SavableHelper.saveDataToNBT(this.blockdata[x][y][z].get(i)));
                    }
                }
            }
        }
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        this.chunkPos = new ChunkCoordIntPair(tagCompound.getInteger("ChunkX"), tagCompound.getInteger("ChunkZ"));
        this.instability = tagCompound.getInteger("instability");

        for (int x = 0; x < this.blockdata.length; x++) {
            for (int y = 0; y < this.blockdata[x].length; y++) {
                for (int z = 0; z < this.blockdata[x][y].length; z++) {
                    if (tagCompound.hasKey("SIZE X:" + x + "Y:" + y + "Z:" + z)) {
                        int Size = tagCompound.getInteger("SIZE X:" + x + "Y:" + y + "Z:" + z);
                        List<BlockSavable> datas = new ArrayList<BlockSavable>();
                        for (int i = 0; i < Size; i++)
                            datas.add(SavableHelper.<BlockSavable>loadDataFromNBT(tagCompound.getCompoundTag("Chunk:" + ChunkCoordIntPair.chunkXZ2Int(this.chunkPos.chunkXPos, this.chunkPos.chunkZPos) + "X:" + x + "Y:" + y + "Z:" + z + "ID:" + i)));
                        this.blockdata[x][y][z] = datas;
                    }
                }
            }
        }
    }
}
