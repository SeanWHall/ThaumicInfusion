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
        this.r = (float) (rgb >> 16 & 255) / 255.0F;
        this.g = (float) (rgb >> 8 & 255) / 255.0F;
        this.b = (float) (rgb & 255) / 255.0F;
    }

    public RGB clamp() {
        if (r < 0)
            r = 0;
        else if (r > 1) r = 1;

        if (g < 0)
            g = 0;
        else if (g > 1) g = 1;

        if (b < 0)
            b = 0;
        else if (b > 1) b = 1;
        return this;
    }

    public RGB lerp(final RGB target, final float t) {
        this.r += t * (target.r - this.r);
        this.g += t * (target.g - this.g);
        this.b += t * (target.b - this.b);
        return clamp();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RGB color = (RGB) o;
        return toIntBits() == color.toIntBits();
    }

    public int toIntBits() {
        return ((int) (255 * b) << 16) | ((int) (255 * g) << 8) | ((int) (255 * r));
    }

    public int getRGB() {
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public void glColor3f() {
        GL11.glColor3f(this.r, this.g, this.b);
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
