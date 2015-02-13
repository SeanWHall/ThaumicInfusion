package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 06/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("tenebrae"), cost = 4)
public class Tenebrae extends AspectEffect {
    public boolean isLit, oldIsLit;

    @SideOnly(Side.CLIENT)
    public void updateBlock(World world) {
        if(!world.isRemote)
            return;

        WorldCoord pos = getPos();
        isLit = world.getBlockLightValue(pos.x, pos.y, pos.z) > 8;

        if (isLit != oldIsLit) {
            oldIsLit = isLit;
            Minecraft.getMinecraft().renderGlobal.markBlockForUpdate(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
        if(isLit)  access.getBlock(x, y, z).setBlockBounds(0, 0, 0, 0, 0, 0);
        else access.getBlock(x, y, z).setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        if(isLit)  return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
        else return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRender(World world, int x, int y, int z, RenderBlocks render) {
        return !isLit;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setByte("isLit", isLit ? (byte) 1 : (byte) 0);
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if(tagCompound.hasKey("isLit"))
            isLit = tagCompound.getByte("isLit") == 1;
    }
}
