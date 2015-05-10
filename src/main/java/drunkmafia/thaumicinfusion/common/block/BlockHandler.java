package drunkmafia.thaumicinfusion.common.block;

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

    private static IBlockHook lastHook;
    private static int lastX, lastY, lastZ;
    private static String lastMethod;

    public static boolean hasWorldData(IBlockAccess access, int x, int y, int z, Block block) {
        return hasWorldData(TIWorldData.getWorld(access), x, y, z, block);
    }

    /**
     * Checks to see if world data exists at a certain block position, returning true will trigger it to invoke the method that called this method. Via the block method
     */
    public static boolean hasWorldData(World world, int x, int y, int z, Block block) {
        if(world == null || block == Blocks.air)
            return false;

        lastX = x;
        lastY = y;
        lastZ = z;
        IBlockHook hook =  TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z));

        if (hook == null)
            return false;

        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName().equals("hasWorldData") ? stackTrace[3].getMethodName() : stackTrace[2].getMethodName();

        for (String blockMethodName : hook.hookMethods(block)) {
            if (methodName.equals(blockMethodName)) {
                BlockHandler.block = hook.getBlock(blockMethodName);
                lastHook = hook;
                lastMethod = methodName;
                return true;
            }
        }

        lastHook = hook;
        lastMethod = methodName;
        return false;
    }

    public static Block getBlock(World world, int x, int y, int z, Chunk chunk){
        Block block = chunk.getBlock(x & 15, y, z & 15);
        if(world.isRemote || blacklistedBlocks.contains(block.getClass().getName()))
            return block;

        IBlockHook hook = TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z));
        if (hook == null)
            return block;

        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        for (String invokerName : materialInvokers) {
            if (!className.equals(invokerName))
                continue;

            for (String hookName : hook.hookMethods(block)) {
                if (hookName.equals(ClassTransformer.getMaterial)) {
                    Block temp = getBlock = hook.getBlock(ClassTransformer.getMaterial);
                    return temp != null ? temp : block;
                }
            }
        }
        return block;
    }

    public static boolean overrideBlockFunctionality(IBlockAccess access, int x, int y, int z) {
        return overrideBlockFunctionality(TIWorldData.getWorld(access), x, y, z);
    }

    public static boolean overrideBlockFunctionality(World world, int x, int y, int z) {
        IBlockHook hook = (lastHook == null || lastX == x || lastY == y || lastZ == z) ? TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z)) : lastHook;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = lastMethod != null ? lastMethod : stackTrace[2].getMethodName().equals("overrideBlockFunctionality") ? stackTrace[3].getMethodName() : stackTrace[2].getMethodName();

        return (!(hook != null && methodName != null)) || hook.shouldOverride(methodName);
    }
}
