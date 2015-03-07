package drunkmafia.thaumicinfusion.common.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.essentiaBlock_RegistryName;

/**
 * Created by DrunkMafia on 01/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TIBlocks {

    public static Block essentiaBlock;

    public static void initBlocks() {
        GameRegistry.registerBlock(essentiaBlock = new EssentiaBlock(), essentiaBlock_RegistryName);


    }
}
