package drunkmafia.thaumicinfusion.common.block;

import cpw.mods.fml.common.registry.GameRegistry;
import drunkmafia.thaumicinfusion.common.block.tile.InscriberTile;
import net.minecraft.block.Block;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.*;

/**
 * Created by DrunkMafia on 01/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIBlocks {

    public static Block essentiaBlock;
    public static Block aspectInscriber;

    public static void initBlocks() {
        GameRegistry.registerBlock(essentiaBlock = new EssentiaBlock(), essentiaBlock_RegistryName);
        GameRegistry.registerBlock(aspectInscriber = new InscriberBlock(), inscriberBlock_RegistryName);

        GameRegistry.registerTileEntity(InscriberTile.class, inscriberBlock_TileEntity);
    }
}
