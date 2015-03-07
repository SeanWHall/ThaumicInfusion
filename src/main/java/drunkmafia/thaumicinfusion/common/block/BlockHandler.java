package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.core.ClassTransformer;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;

/**
 * Created by DrunkMafia on 04/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public final class BlockHandler {

    public static Block block;

    public static boolean hasWorldData(IBlockAccess access, int x, int y, int z) {
        World world = TIWorldData.getWorld(access);
        return world != null && hasWorldData(world, x, y, z);
    }

    public static boolean hasWorldData(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) == Blocks.air)
            return false;

        IBlockHook hook = TIWorldData.getWorldData(world).getBlock(IBlockHook.class, new WorldCoord(x, y, z));

        if (hook == null)
            return false;

        StackTraceElement blockMethod = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement aStackTrace : stackTrace) {
            blockMethod = aStackTrace;
            for (String blockMethodName : ClassTransformer.blockMethods) {
                if (blockMethod.getMethodName().equals(blockMethodName))
                    break;
            }
            if (blockMethod != null)
                break;
        }

        return !(blockMethod == null || !(Arrays.asList(hook.hookMethods()).contains(blockMethod.getMethodName()))) && (block = hook.getBlock(blockMethod.getMethodName())) != null;

    }
}
