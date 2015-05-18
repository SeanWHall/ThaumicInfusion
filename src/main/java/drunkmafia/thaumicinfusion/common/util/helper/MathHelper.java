package drunkmafia.thaumicinfusion.common.util.helper;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by DrunkMafia on 31/10/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public final class MathHelper {

    public static float lerp(float current, float target, float tick) {
        return current + tick * (target - current);
    }


    public static ForgeDirection sideToDirection(int side) {
        return side == 0 ? ForgeDirection.DOWN : side == 1 ? ForgeDirection.UP : side == 2 ? ForgeDirection.NORTH : side == 3 ? ForgeDirection.SOUTH : side == 4 ? ForgeDirection.WEST : ForgeDirection.UNKNOWN;
    }

    public static boolean withinThreshold(float a, float b, float threshold){
        return Math.abs(a - b) < threshold;
    }
}
