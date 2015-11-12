/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.lib;

import net.minecraft.util.ResourceLocation;

public class BlockInfo {

    public static final String infusedBlock_UnlocalizedName = "local_infused";
    public static final String infusedBlock_RegistryName = "reg_infused";
    public static final String essentiaBlock_UnlocalizedName = "local_essentia";
    public static final String essentiaBlock_RegistryName = "reg_essentia";
    public static final String inscriberBlock_UnlocalizedName = "local_inscriber";
    public static final String inscriberBlock_RegistryName = "reg_inscriber";
    public static final String inscriberBlock_TileEntity = "tile_inscriber";
    public static final ResourceLocation inscriber_Texture = new ResourceLocation(ModInfo.MODID, "textures/models/Inscriber.png");
    public static final ResourceLocation inscriber_Model = new ResourceLocation(ModInfo.MODID, "models/Inscriber.obj");
    public static final String infusionCoreBlock_UnlocalizedName = "local_infusionCore";
    public static final String infusionCoreBlock_RegistryName = "reg_infusionCore";
    public static final String infusionCoreBlock_TileEntity = "tile_infusionCore";
    public static final ResourceLocation infusionCore_Texture = new ResourceLocation(ModInfo.MODID, "textures/models/asm.png");
    public static final ResourceLocation infusionCoreBlock_Model = new ResourceLocation(ModInfo.MODID, "models/asm.obj");
    private static final String TEXTUREBASE = ModInfo.MODID + ":";
    public static final String infusedBlock_BlankTexture = BlockInfo.TEXTUREBASE + "blank";
    public static final String essentiaBlock_BlockTexture = BlockInfo.TEXTUREBASE + "essentiablock";
    public static final String essentiaBlock_BrickTexture = BlockInfo.TEXTUREBASE + "bricks_essentiablock";
    public static final String essentiaBlock_SquareTexture = BlockInfo.TEXTUREBASE + "squarebrick_essentiablock";
}
