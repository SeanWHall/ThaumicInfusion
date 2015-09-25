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

/**
 * A wrapper class between Blocks and Effects, the following methods are invoked by every block method & so have been optimized
 * with speed in mind, the approximate run time for this is: 1400 NS with a successful detection & invocation of the aspects code.
 * Meaning the performance impact that TI has on blocks is negligible, this is ridiculously fast that vanillas own getBlock Method.
 */
public final class BlockWrapper {

    /**
     * Accessed by block methods to invoke code dynamically, this is done over storing the object locally in the methods
     * as TI does not want to alter the block methods anymore than is required. Which means there is less room for error.
     */
    public static Block block;

    private static IBlockHook lastHook;
    private static int lastX, lastY, lastZ;

    /**
     * Used to decided if an effects exists in this blocks position
     * @return If true then it triggers the ASM code within the block to run the effects code
     */
    public static boolean hasWorldData(IBlockAccess access, int x, int y, int z, Block block, int methodHash) {
        World world = TIWorldData.getWorld(access);
        if (world == null || block == Blocks.air)
            return false;

        TIWorldData worldData = TIWorldData.getWorldData(world);
        if (worldData == null) return false;

        IBlockHook hook = worldData.getBlock(IBlockHook.class, new WorldCoordinates(x, y, z, world.provider.dimensionId));

        if (hook == null)
            return false;

        for (int method : hook.hookMethods(block)) {
            if (method == methodHash) {
                BlockWrapper.block = hook.getBlock(methodHash);
                lastHook = hook;
                lastX = x;
                lastY = y;
                lastZ = z;
                return true;
            }
        }
        return false;
    }

    /**
     * Used to decide if an effects functionality should override the blocks own functionality
     *
     * @return If true then it stops the blocks code from running and returns the value
     */
    public static boolean overrideBlockFunctionality(IBlockAccess access, int x, int y, int z, int methodName) {
        World world = TIWorldData.getWorld(access);
        IBlockHook hook = (lastHook == null || lastX == x || lastY == y || lastZ == z) ? TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoordinates(x, y, z, world.provider.dimensionId)) : lastHook;
        return hook != null && hook.shouldOverride(methodName);
    }
}
