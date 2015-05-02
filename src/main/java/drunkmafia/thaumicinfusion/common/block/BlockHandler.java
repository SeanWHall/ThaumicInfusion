package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.core.ClassTransformer;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by DrunkMafia on 04/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public final class BlockHandler {
    /**
     * Accessed by block methods to invoke code dynamically
     */
    public static Block block, getBlock;
    public static ArrayList<String> materialInvokers = new ArrayList<String>();
    public static List<String> blacklistedBlocks = Arrays.asList(BlockAir.class.getName());
    public static boolean hasWorldData(IBlockAccess access, int x, int y, int z, Block block, String methodName) {
        return hasWorldData(TIWorldData.getWorld(access), x, y, z, block, methodName);
    }

    /**
     * Checks to see if world data exists at a certain block position, returning true will trigger it to invoke the method that called this method. Via the block method
     */
    public static boolean hasWorldData(World world, int x, int y, int z, Block block, String methodName) {
        if(world == null || block == Blocks.air)
            return false;

        IBlockHook hook =  TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z));

        if (hook != null) {
            for (String blockMethodName : hook.hookMethods()) {
                if (methodName.equals(blockMethodName)) {

                    BlockHandler.block = hook.getBlock(methodName);
                    return true;
                }
            }
        }
        return false;
    }

    public static Block getBlock(World world, int x, int y, int z, Chunk chunk){
        Block block = chunk.getBlock(x & 15, y, z & 15);
        if(world.isRemote || blacklistedBlocks.contains(block.getClass().getName()))
            return block;

        IBlockHook hook = TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z));
        if (hook == null)
            return block;


        if (materialInvokers.contains(Thread.currentThread().getStackTrace()[3].getMethodName())) {
            for (String hookName : hook.hookMethods()) {
                if (hookName.equals(ClassTransformer.getMaterial)) {
                    Block temp = getBlock = hook.getBlock(ClassTransformer.getMaterial);
                    return temp != null ? temp : block;
                }
            }
        }
        return block;
    }
}
