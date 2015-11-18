/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.internal.WorldCoordinates;

@Effect(aspect = ("lux"), cost = 1)
public class Lux extends AspectEffect {

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (world.isRemote)
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.pos);
    }

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote)
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos);
    }

    @Override
    @BlockMethod
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return 14;
    }
}
