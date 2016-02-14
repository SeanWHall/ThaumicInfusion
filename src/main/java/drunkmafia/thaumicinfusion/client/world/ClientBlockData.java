package drunkmafia.thaumicinfusion.client.world;

import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.client.util.RGB;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.helper.MathHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
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
    private EffectModel model;
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
                model = new EffectModel(TIWorldData.getWorldData(player.worldObj), getCoords());
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

            model.Block.render(0.0315F);

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

        public EffectModel(TIWorldData worldData, WorldCoordinates pos) {
            this.textureWidth = 32;
            this.textureHeight = 32;
            this.Block = new ModelRenderer(this, 0, 0);
            SidedBox box = new SidedBox(Block, 0, 0, -16, -16, -16, 32, 32, 32, 0.0F);
            box.worldData = worldData;
            box.pos = pos;

            this.Block.cubeList.add(box);
            this.Block.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.Block.setTextureSize(32, 32);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class SidedBox extends ModelBox {

        /**
         * X vertex coordinate of lower box corner
         */
        public float posX1;
        /**
         * Y vertex coordinate of lower box corner
         */
        public float posY1;
        /**
         * Z vertex coordinate of lower box corner
         */
        public float posZ1;
        /**
         * X vertex coordinate of upper box corner
         */
        public float posX2;
        /**
         * Y vertex coordinate of upper box corner
         */
        public float posY2;
        /**
         * Z vertex coordinate of upper box corner
         */
        public float posZ2;
        public TIWorldData worldData;
        public WorldCoordinates pos;
        /**
         * The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube
         */
        private PositionTextureVertex[] vertexPositions;
        /**
         * An array of 6 TexturedQuads, one for each face of a cube
         */
        private TexturedQuad[] quadList;

        public SidedBox(ModelRenderer renderer, int p_i46359_2_, int p_i46359_3_, float p_i46359_4_, float p_i46359_5_, float p_i46359_6_, int p_i46359_7_, int p_i46359_8_, int p_i46359_9_, float p_i46359_10_) {
            this(renderer, p_i46359_2_, p_i46359_3_, p_i46359_4_, p_i46359_5_, p_i46359_6_, p_i46359_7_, p_i46359_8_, p_i46359_9_, p_i46359_10_, renderer.mirror);
        }

        public SidedBox(ModelRenderer renderer, int textureX, int textureY, float p_i46301_4_, float p_i46301_5_, float p_i46301_6_, int p_i46301_7_, int p_i46301_8_, int p_i46301_9_, float p_i46301_10_, boolean p_i46301_11_) {
            super(renderer, textureX, textureY, p_i46301_4_, p_i46301_5_, p_i46301_6_, p_i46301_7_, p_i46301_8_, p_i46301_9_, p_i46301_10_, p_i46301_11_);
            this.posX1 = p_i46301_4_;
            this.posY1 = p_i46301_5_;
            this.posZ1 = p_i46301_6_;
            this.posX2 = p_i46301_4_ + (float) p_i46301_7_;
            this.posY2 = p_i46301_5_ + (float) p_i46301_8_;
            this.posZ2 = p_i46301_6_ + (float) p_i46301_9_;
            this.vertexPositions = new PositionTextureVertex[8];
            this.quadList = new TexturedQuad[6];
            float f = p_i46301_4_ + (float) p_i46301_7_;
            float f1 = p_i46301_5_ + (float) p_i46301_8_;
            float f2 = p_i46301_6_ + (float) p_i46301_9_;
            p_i46301_4_ = p_i46301_4_ - p_i46301_10_;
            p_i46301_5_ = p_i46301_5_ - p_i46301_10_;
            p_i46301_6_ = p_i46301_6_ - p_i46301_10_;
            f = f + p_i46301_10_;
            f1 = f1 + p_i46301_10_;
            f2 = f2 + p_i46301_10_;

            if (p_i46301_11_) {
                float f3 = f;
                f = p_i46301_4_;
                p_i46301_4_ = f3;
            }

            PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(p_i46301_4_, p_i46301_5_, p_i46301_6_, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f, p_i46301_5_, p_i46301_6_, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f, f1, p_i46301_6_, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(p_i46301_4_, f1, p_i46301_6_, 8.0F, 0.0F);
            PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(p_i46301_4_, p_i46301_5_, f2, 0.0F, 0.0F);
            PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f, p_i46301_5_, f2, 0.0F, 8.0F);
            PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
            PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(p_i46301_4_, f1, f2, 8.0F, 0.0F);
            this.vertexPositions[0] = positiontexturevertex7;
            this.vertexPositions[1] = positiontexturevertex;
            this.vertexPositions[2] = positiontexturevertex1;
            this.vertexPositions[3] = positiontexturevertex2;
            this.vertexPositions[4] = positiontexturevertex3;
            this.vertexPositions[5] = positiontexturevertex4;
            this.vertexPositions[6] = positiontexturevertex5;
            this.vertexPositions[7] = positiontexturevertex6;

            this.quadList[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, textureX + p_i46301_9_, textureY, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, renderer.textureWidth, renderer.textureHeight);
            this.quadList[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_7_, textureY, renderer.textureWidth, renderer.textureHeight);
            this.quadList[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, textureX + p_i46301_9_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
            this.quadList[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
            this.quadList[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, textureX, textureY + p_i46301_9_, textureX + p_i46301_9_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
            this.quadList[5] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);


            if (p_i46301_11_) {
                for (TexturedQuad aQuadList : this.quadList) {
                    aQuadList.flipFace();
                }
            }
        }

        @Override
        public void render(WorldRenderer renderer, float scale) {
            for (int i = 0; i < this.quadList.length; ++i) {
                EnumFacing dir = EnumFacing.values()[i];
                if (worldData.getBlock(BlockData.class, pos.pos.add(dir.getFrontOffsetX(), dir.getFrontOffsetY(), dir.getFrontOffsetZ())) != null)
                    continue;

                if (this.quadList[i] != null) this.quadList[i].draw(renderer, scale);
            }
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
