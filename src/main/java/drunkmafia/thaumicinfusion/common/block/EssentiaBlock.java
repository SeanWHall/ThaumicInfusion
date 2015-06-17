/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.EssentiaData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.*;

public class EssentiaBlock extends Block {

    @SideOnly(Side.CLIENT)
    private IIcon brick;
    @SideOnly(Side.CLIENT)
    private IIcon squarebrick;

    public EssentiaBlock() {
        super(Material.rock);
        setCreativeTab(ThaumicInfusion.instance.tab);
        setBlockName(essentiaBlock_UnlocalizedName);
        setHardness(1.5F);
        setLightLevel(1F);
        setResistance(10.0F);
    }

    public static ItemStack getEssentiaBlock(Aspect aspect, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("aspectTag", aspect.getTag());
        ItemStack stack = new ItemStack(TIBlocks.essentiaBlock);
        stack.setItemDamage(meta);
        stack.setTagCompound(tag);
        stack.setStackDisplayName(aspect.getName() + (meta != 0 ? (meta == 1 ? ThaumicInfusion.translate("key.essentiaBlock.brick") : ThaumicInfusion.translate("key.essentiaBlock.chiseled")) : ""));
        return stack;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        Object[] objs = Aspect.aspects.entrySet().toArray();
        for(Object obj : objs){
            for(int i = 0; i <= 2; i++) {
                NBTTagCompound tag = new NBTTagCompound();
                Aspect aspect = (Aspect) ((Map.Entry) obj).getValue();
                tag.setString("aspectTag", aspect.getTag());
                ItemStack stack = new ItemStack(this);
                stack.setItemDamage(i);
                stack.setTagCompound(tag);
                stack.setStackDisplayName(aspect.getName() + (i != 0 ? (i == 1 ? " Brick" : " chiseled") : ""));
                list.add(stack);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        blockIcon = icon.registerIcon(essentiaBlock_BlockTexture);
        brick = icon.registerIcon(essentiaBlock_BrickTexture);
        squarebrick = icon.registerIcon(essentiaBlock_SquareTexture);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        switch (meta){
            case 1: return brick;
            case 2: return squarebrick;
            default: return blockIcon;
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        EssentiaData data = TIWorldData.getWorldData(world).getBlock(EssentiaData.class, new WorldCoordinates(x, y, z, player.dimension));
        if(data != null) {
            int meta = world.getBlockMetadata(x, y, z);
            ItemStack stack = new ItemStack(this, 1, meta);
            NBTTagCompound tagCompound = new NBTTagCompound();

            Aspect aspect = data.getAspect();
            tagCompound.setString("aspectTag", aspect.getTag());
            stack.setTagCompound(tagCompound);
            stack.setStackDisplayName(aspect.getName() + (meta != 0 ? (meta == 1 ? " Brick" : " chiseled") : ""));

            return stack;
        }
        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        TIWorldData worldData = TIWorldData.getWorldData(world);
        WorldCoordinates coord = new WorldCoordinates(x, y, z, entity.dimension);

        world.setBlockMetadataWithNotify(coord.x, coord.y, coord.z, stack.getItemDamage(), 3);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if(tagCompound != null)
            worldData.addBlock(new EssentiaData(coord, Aspect.getAspect(tagCompound.getString("aspectTag"))));
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        EssentiaData data = TIWorldData.getWorldData(world).getBlock(EssentiaData.class, new WorldCoordinates(x, y, z, world.provider.dimensionId));

        int meta = world.getBlockMetadata(x, y, z);
        ItemStack stack = new ItemStack(TIBlocks.essentiaBlock, 1, meta);

        NBTTagCompound tagCompound = new NBTTagCompound();
        Aspect aspect = data.getAspect();
        tagCompound.setString("aspectTag", aspect.getTag());
        stack.setTagCompound(tagCompound);
        stack.setStackDisplayName(aspect.getName() + (meta != 0 ? (meta == 1 ? " Brick" : " chiseled") : ""));

        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int id) {}

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
        if (!world.isRemote)
            TIWorldData.getWorldData(world).removeData(EssentiaData.class, new WorldCoordinates(x, y, z, world.provider.dimensionId), true);
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess access, int x, int y, int z){
        EssentiaData data = TIWorldData.getWorldData(TIWorldData.getWorld(access)).getBlock(EssentiaData.class, new WorldCoordinates(x, y, z, Minecraft.getMinecraft().thePlayer.dimension));
        if(data == null || data.getAspect() == null)
            return 0;
        return data.getAspect().getColor();
    }
}
