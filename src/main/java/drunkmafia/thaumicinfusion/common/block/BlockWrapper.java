/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

/**
 * A wrapper class between Blocks and Effects, the following methods are invoked by every block method & so have been optimized
 * with speed in mind, the approximate run time for this is: 1400 NS with a successful detection & invocation of the aspects code.
 * Meaning the performance impact that TI has on blocks is negligible, this is ridiculously fast that vanillas own getBlock Method.
 */
public final class BlockWrapper {

    public static long handlerTime;

    /**
     * Accessed by block methods to invoke code dynamically, this is done over storing the object locally in the methods
     * as TI does not want to alter the block methods anymore than is required. Which means there is less room for error.
     */
    public static Block block;
    public static boolean suppressed = false;
    private static IBlockHook lastHook;
    private static BlockPos lastPos;

    /**
     * Used to decided if an effects exists in this blocks position
     *
     * @return If true then it triggers the ASM code within the block to run the effects code
     */
    public static boolean hasWorldData(IBlockAccess access, BlockPos pos, Block block, int methodIndex) {
        World world = TIWorldData.getWorld(access);

        //Checks to increase performance, Air blocks are not infuseable & it checks if the same method is being called at the same positions
        //Which deals with super calls by blocks, that way the effect is not invoked multiple times by the same block
        //Also check to see if the position has been marked as suppressed
        if (world == null || pos == null || block == Blocks.air || block instanceof AspectEffect || isSuppressed())
            return false;

        long start = System.nanoTime();

        TIWorldData worldData = TIWorldData.getWorldData(world);
        if (worldData == null) return false;

        IBlockHook hook = worldData.getBlock(IBlockHook.class, new WorldCoordinates(pos, world.provider.getDimensionId()));

        if (hook == null) return false;


        Block efectBlock = hook.getBlock(methodIndex);


        if (efectBlock == null) return false;

        BlockWrapper.block = efectBlock;
        BlockWrapper.lastHook = hook;
        BlockWrapper.lastPos = pos;

        handlerTime = System.nanoTime() - start;

        //Ensures that the block method does not try to invoke its method with a null Block
        return BlockWrapper.block != null;
    }

    /**
     * Checks to see if the current hook is being suppressed
     *
     * @return If hook should be suppressed
     */
    private static boolean isSuppressed() {
        boolean ret = suppressed;
        suppressed = false;
        return ret;
    }

    /**
     * Used to decide if an effects functionality should override the blocks own functionality
     *
     * @return If true then it stops the blocks code from running and returns the value
     */
    public static boolean overrideBlockFunctionality(IBlockAccess access, BlockPos pos, int methodName) {
        World world = TIWorldData.getWorld(access);
        TIWorldData worldData = TIWorldData.getWorldData(world);

        if (worldData == null) {
            ThaumicInfusion.getLogger().error("Failed to get World data when it should be present, resorting to last known data. This could have averse effects");
            return lastHook != null && lastHook.shouldOverride(methodName);
        }

        IBlockHook hook = lastHook == null || lastPos.equals(pos) ? worldData.getBlock(IBlockHook.class, new WorldCoordinates(pos, world.provider.getDimensionId())) : BlockWrapper.lastHook;
        return hook != null && hook.shouldOverride(methodName);
    }
}
