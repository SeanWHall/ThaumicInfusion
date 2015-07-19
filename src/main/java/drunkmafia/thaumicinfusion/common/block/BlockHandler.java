/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

public final class BlockHandler {
    /**
     * Accessed by block methods to invoke code dynamically, this is done over storing the object locally in the methods
     * as TI does not want to alter the block methods anymore than is required. Which means there is less room for error.
     */
    public static Block block;

    private static IBlockHook lastHook;
    private static int lastX, lastY, lastZ;
    private static String lastMethod;

    public static boolean hasWorldData(IBlockAccess access, int x, int y, int z, Block block) {
        return hasWorldData(TIWorldData.getWorld(access), x, y, z, block);
    }

    /**
     * Used to decided if an effects exists in this blocks position
     * @return If true then it triggers the ASM code within the block to run the effects code
     */
    public static boolean hasWorldData(World world, int x, int y, int z, Block block) {
        if (world == null || block == Blocks.air)
            return false;

        TIWorldData worldData = TIWorldData.getWorldData(world);
        if (worldData == null) return false;

        IBlockHook hook = worldData.getBlock(IBlockHook.class, new WorldCoordinates(x, y, z, world.provider.dimensionId));

        if (hook == null)
            return false;

        //TODO Replace this with an integer passed in as a par, which is the indx of the correct method.
        //Should increase the overall perforamnce of this method.
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[2].getMethodName().equals("hasWorldData") ? stackTrace[3].getMethodName() : stackTrace[2].getMethodName();

        for (String blockMethodName : hook.hookMethods(block)) {
            if (methodName.equals(blockMethodName)) {
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

    /**
     * Used to decidede if an effects functionality should override the blocks own functionality
     *
     * @return If true then it stops the blocks code from running and returns the value
     */
    public static boolean overrideBlockFunctionality(World world, int x, int y, int z) {
        IBlockHook hook = (lastHook == null || lastX == x || lastY == y || lastZ == z) ? TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoordinates(x, y, z, world.provider.dimensionId)) : lastHook;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = lastMethod != null ? lastMethod : stackTrace[2].getMethodName().equals("overrideBlockFunctionality") ? stackTrace[3].getMethodName() : stackTrace[2].getMethodName();

        return (!(hook != null && methodName != null)) || hook.shouldOverride(methodName);
    }
}
