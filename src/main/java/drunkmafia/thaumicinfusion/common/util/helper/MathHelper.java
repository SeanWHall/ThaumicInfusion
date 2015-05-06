package drunkmafia.thaumicinfusion.common.util.helper;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by DrunkMafia on 31/10/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class MathHelper {

    public static float clamp(float val, float maxClamp, float minClamp){
        return Math.max(minClamp, Math.min(val, maxClamp));
    }

    public static float lerp(float to, float from, float f){
        float ret = (to > from ? to - f : to + f);
        if(withinThreshold(from, ret, 1))
            return from;
        return ret;
    }

    public static float lerp(float to, float from, float f, float threshold){
        float ret = (to > from ? to - f : to + f);
        if(withinThreshold(from, ret, threshold))
            return from;
        return ret;
    }

    public static ForgeDirection sideToDirection(int side) {
        return side == 0 ? ForgeDirection.DOWN : side == 1 ? ForgeDirection.UP : side == 2 ? ForgeDirection.NORTH : side == 3 ? ForgeDirection.SOUTH : side == 4 ? ForgeDirection.WEST : ForgeDirection.UNKNOWN;
    }

    public static boolean withinThreshold(float a, float b, float threshold){
        return Math.abs(a - b) < threshold;
    }
}
