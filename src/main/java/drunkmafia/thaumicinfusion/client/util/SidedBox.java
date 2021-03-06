package drunkmafia.thaumicinfusion.client.util;

import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.internal.WorldCoordinates;

@SideOnly(Side.CLIENT)
public class SidedBox extends ModelBox {

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
        /*
      The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube
     */
        PositionTextureVertex[] vertexPositions = new PositionTextureVertex[8];
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
        vertexPositions[0] = positiontexturevertex7;
        vertexPositions[1] = positiontexturevertex;
        vertexPositions[2] = positiontexturevertex1;
        vertexPositions[3] = positiontexturevertex2;
        vertexPositions[4] = positiontexturevertex3;
        vertexPositions[5] = positiontexturevertex4;
        vertexPositions[6] = positiontexturevertex5;
        vertexPositions[7] = positiontexturevertex6;

        this.quadList[0] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, textureX + p_i46301_9_, textureY, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, renderer.textureWidth, renderer.textureHeight);
        this.quadList[1] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_7_, textureY, renderer.textureWidth, renderer.textureHeight);
        this.quadList[2] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, textureX + p_i46301_9_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
        this.quadList[3] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
        this.quadList[4] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, textureX, textureY + p_i46301_9_, textureX + p_i46301_9_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);
        this.quadList[5] = new TexturedQuad(new PositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, textureX + p_i46301_9_ + p_i46301_7_, textureY + p_i46301_9_, textureX + p_i46301_9_ + p_i46301_7_ + p_i46301_9_, textureY + p_i46301_9_ + p_i46301_8_, renderer.textureWidth, renderer.textureHeight);


        if (p_i46301_11_) {
            for (TexturedQuad aQuadList : this.quadList)
                aQuadList.flipFace();
        }
    }

    @Override
    public void render(WorldRenderer renderer, float scale) {
        for (int i = 0; i < this.quadList.length; ++i) {
            if (worldData.getBlock(BlockData.class, pos.pos.add(EnumFacing.values()[i].getDirectionVec())) != null)
                continue;

            if (this.quadList[i] != null) this.quadList[i].draw(renderer, scale);
        }
    }
}
