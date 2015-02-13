package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "corpus", cost = 1)
public class Corpus extends AspectEffect {

    private int foodStatus = 0;

    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float f = 0.0625F;
        float f1 = (float)(1 + foodStatus * 2) / 16.0F;
        float f2 = 0.5F;
        this.setBlockBounds(f1, 0.0F, f, 1.0F - f, f2, 1.0F - f);
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        float f = 0.0625F;
        float f1 = (float)(1 + foodStatus * 2) / 16.0F;
        float f2 = 0.5F;
        return AxisAlignedBB.getBoundingBox((double)((float)x + f1), (double)y, (double)((float)z + f), (double)((float)(x + 1) - f), (double)((float)y + f2 - f), (double)((float)(z + 1) - f));
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        float f = 0.0625F;
        float f1 = (float)(1 + foodStatus * 2) / 16.0F;
        float f2 = 0.5F;
        return AxisAlignedBB.getBoundingBox((double)((float)x + f1), (double)y, (double)((float)z + f), (double)((float)(x + 1) - f), (double)((float)y + f2), (double)((float)(z + 1) - f));
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return eatBlock(world, x, y, z, player) || data.getContainingBlock().onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        eatBlock(world, x, y, z, player);
    }

    private boolean eatBlock(World world, int x, int y, int z, EntityPlayer player) {
        if (player.canEat(true))
            return false;

        player.getFoodStats().addStats(2, 0.1F);
        foodStatus++;
        if (foodStatus >= 6) {
            TIWorldData.getWorldData(world).removeBlock(getPos());
            world.setBlockToAir(x, y, z);
        }
        return true;
    }
}
