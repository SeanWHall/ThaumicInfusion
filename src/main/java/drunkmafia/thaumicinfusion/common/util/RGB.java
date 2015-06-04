/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util;

import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RGB {
    private float r, g, b;

    public RGB(){
        Random rand = new Random();
        r = rand.nextFloat();
        g = rand.nextFloat();
        b = rand.nextFloat();
    }

    public RGB(float r, float g, float b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public RGB(int rgb){
        r = (float)(rgb >> 16 & 255) / 255.0F;
        g = (float)(rgb >> 8 & 255) / 255.0F;
        b = (float)(rgb & 255) / 255.0F;
    }

    public void addRGB(RGB rgb){
        r += Math.min(255, rgb.r);
        g += Math.min(255, rgb.g);
        b += Math.min(255, rgb.b);
    }

    public void glColor3f(){
        GL11.glColor3f(r, g, b);
    }

    public float getB() {
        return b;
    }

    public float getG() {
        return g;
    }

    public float getR() {
        return r;
    }
}
