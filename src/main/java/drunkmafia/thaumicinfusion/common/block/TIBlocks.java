/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.essentiaBlock_RegistryName;

public class TIBlocks {

    public static Block essentiaBlock;
    public static Block aspectInscriber;

    public static void initBlocks() {
        GameRegistry.registerBlock(TIBlocks.essentiaBlock = new EssentiaBlock(), essentiaBlock_RegistryName);
    }
}
