/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TIBlocks {

    public static Block fakeAirBlock;
    public static Block fakeAirWithCollisionBlock;

    public static void initBlocks() {
        fakeAirBlock = GameRegistry.registerBlock(new FakeAirBlock(false), "fake_air");
        fakeAirWithCollisionBlock = GameRegistry.registerBlock(new FakeAirBlock(true), "fake_air_collision");
    }
}
