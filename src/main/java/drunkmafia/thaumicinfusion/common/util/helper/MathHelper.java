/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util.helper;

import net.minecraft.util.EnumFacing;

public final class MathHelper {

    public static float lerp(float current, float target, float tick) {
        return current + tick * (target - current);
    }


    public static EnumFacing sideToDirection(int side) {
        return side == 0 ? EnumFacing.DOWN : side == 1 ? EnumFacing.UP : side == 2 ? EnumFacing.NORTH : side == 3 ? EnumFacing.SOUTH : side == 4 ? EnumFacing.WEST : null;
    }

    public static boolean withinThreshold(float a, float b, float threshold) {
        return Math.abs(a - b) < threshold;
    }
}
