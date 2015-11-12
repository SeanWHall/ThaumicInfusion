/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

@Effect(aspect = "lux")
public class Lux extends AspectEffect {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (world.isRemote)
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public boolean shouldDrain() {
        return false;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world.isRemote)
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(this.pos.x, this.pos.y, this.pos.z);
    }

    @OverrideBlock
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return 14;
    }
}
