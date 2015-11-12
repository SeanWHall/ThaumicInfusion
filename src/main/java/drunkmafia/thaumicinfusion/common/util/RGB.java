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
    private int rgb;

    public RGB() {
        Random rand = new Random();
        this.r = rand.nextFloat();
        this.g = rand.nextFloat();
        this.b = rand.nextFloat();
    }

    public RGB(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public RGB(int rgb) {
        this.rgb = rgb;
        this.r = (float) (rgb >> 16 & 255) / 255.0F;
        this.g = (float) (rgb >> 8 & 255) / 255.0F;
        this.b = (float) (rgb & 255) / 255.0F;
    }

    public void addRGB(RGB rgb) {
        this.r += Math.min(255, rgb.r);
        this.g += Math.min(255, rgb.g);
        this.b += Math.min(255, rgb.b);
    }

    public void takeRGB(RGB rgb) {
        this.r += Math.min(0, rgb.r);
        this.g += Math.min(0, rgb.g);
        this.b += Math.min(0, rgb.b);
    }

    public void glColor3f() {
        GL11.glColor3f(this.r, this.g, this.b);
    }

    public float getRGB() {
        return this.rgb;
    }

    public float getB() {
        return this.b;
    }

    public float getG() {
        return this.g;
    }

    public float getR() {
        return this.r;
    }
}
