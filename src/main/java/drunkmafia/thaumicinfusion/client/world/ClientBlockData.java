package drunkmafia.thaumicinfusion.client.world;

import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.util.RGB;
import drunkmafia.thaumicinfusion.client.util.SidedBox;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.helper.MathHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.common.items.wands.ItemWand;

@SideOnly(Side.CLIENT)
public class ClientBlockData extends BlockData implements IClientRenderer {

    @SideOnly(Side.CLIENT)
    private static ResourceLocation[] textures;

    //Client Variables
    //private int[] methods = new int[0];
    private int tick, colour = 0;
    @SideOnly(Side.CLIENT)
    private AnimatedFrames infusionFrames;
    @SideOnly(Side.CLIENT)
    private float alpha;

    @Override
    public boolean shouldRender(EntityPlayer player) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void renderData(EntityPlayer player, float partialTicks) {
        int x = this.coordinates.pos.getX(), y = this.coordinates.pos.getY(), z = this.coordinates.pos.getZ();
        double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
        double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        float targetAlpha = (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemWand.class) && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) != null && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) ? 0.7F : 0;

        if (alpha != 0 || targetAlpha == 0.7F) {
            alpha = MathHelper.lerp(alpha, targetAlpha, 0.05F * partialTicks);

            if (infusionFrames == null) {
                if (textures == null) {
                    textures = new ResourceLocation[6];
                    for (int i = 0; i < textures.length; i++)
                        textures[i] = new ResourceLocation(ModInfo.MODID, "textures/blocks/infusion/" + (i + 1) + ".png");
                }
                infusionFrames = new AnimatedFrames(textures, 60);
            }

            if (!Minecraft.getMinecraft().isGamePaused()) {
                if (getAspects().length > 1) {
                    if (tick >= 500) {
                        int lastColour = colour;
                        boolean getNewColour = false;
                        for (Aspect aspect : getAspects()) {
                            if (getNewColour) {
                                colour = aspect.getColor();
                                getNewColour = false;
                                break;
                            }
                            if (aspect.getColor() == lastColour) getNewColour = true;
                        }
                        if (getNewColour) colour = getAspects()[0].getColor();
                        tick = 0;
                    } else {
                        tick++;
                    }
                }
            }

            if (colour == 0) colour = getAspects()[0].getColor();

            RGB rgb = new RGB(colour);

            GL11.glPushMatrix();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(rgb.getR(), rgb.getG(), rgb.getB(), alpha);

            Minecraft.getMinecraft().getTextureManager().bindTexture(infusionFrames.getTexture());
            GL11.glTranslated(-iPX + x + 0.5D, -iPY + y + 0.5D, -iPZ + z + 0.5D);

            new EffectModel(TIWorldData.getWorldData(player.worldObj), getCoords()).Block.render(0.0315F);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        for (AspectEffect effect : this.getEffects()) {
            if (effect instanceof IClientRenderer)
                ((IClientRenderer) effect).renderData(player, partialTicks);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class EffectModel extends ModelBase {
        public ModelRenderer Block;
        public SidedBox box;

        public EffectModel(TIWorldData worldData, WorldCoordinates pos) {
            this.textureWidth = 32;
            this.textureHeight = 32;
            this.Block = new ModelRenderer(this, 0, 0);
            box = new SidedBox(Block, 0, 0, -16, -16, -16, 32, 32, 32, 0.0F);
            box.worldData = worldData;
            box.pos = pos;

            this.Block.cubeList.add(box);
            this.Block.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.Block.setTextureSize(32, 32);
        }
    }

    @SideOnly(Side.CLIENT)
    static class AnimatedFrames {

        int currentFrame = 0, fps, frameCounter = 0;
        private ResourceLocation[] frames;

        public AnimatedFrames(ResourceLocation[] frames, int fps) {
            this.frames = frames;
            this.fps = fps;
        }

        public ResourceLocation getTexture() {
            ResourceLocation response = frames[currentFrame];
            if (!Minecraft.getMinecraft().isGamePaused()) {
                frameCounter++;
                if (frameCounter >= fps) {
                    currentFrame++;
                    frameCounter = 0;
                    if (currentFrame >= frames.length) currentFrame = 0;
                }
            }
            return response;
        }
    }
}
