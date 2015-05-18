package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 04/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public final class BlockHandler {
    /**
     * Accessed by block methods to invoke code dynamically
     */
    public static Block block;

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
        if (world == null)
            return false;

        IBlockHook hook = (x != lastX || y != lastY || z != lastZ) ? TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z)) : lastHook;

        if (hook == null)
            return false;

        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName().equals("hasWorldData") ? stackTrace[3].getMethodName() : stackTrace[2].getMethodName();

        for (String blockMethodName : hook.hookMethods(block)) {
            if (methodName.equals(blockMethodName) && (!world.isAirBlock(x, y, z) || hook.shouldHookWhenAir(blockMethodName))) {
                BlockHandler.block = hook.getBlock(blockMethodName);
                lastHook = hook;
                lastMethod = methodName;
                lastX = x;
                lastY = y;
                lastZ = z;
                return true;
            }
        }
        return false;
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
