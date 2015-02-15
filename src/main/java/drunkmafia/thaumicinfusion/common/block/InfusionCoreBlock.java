package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.block.tile.InfusionCoreTile;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.common.blocks.BlockStoneDevice;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TilePedestal;

import java.util.List;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.infusionCoreBlock_UnlocalizedName;

/**
 * Created by DrunkMafia on 19/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class InfusionCoreBlock extends BlockStoneDevice implements IInfusionStabiliser {

    protected InfusionCoreBlock() {
        setBlockName(infusionCoreBlock_UnlocalizedName);
        setCreativeTab(ThaumicInfusion.instance.tab);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        world.setBlockMetadataWithNotify(x, y, z, 1, 3);
        world.setTileEntity(x, y, z, createNewTileEntity(world, 1));
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        InfusionCoreTile core = (InfusionCoreTile) world.getTileEntity(x, y, z);
        if(core != null)
            setBlockBounds(0.25F, 0.55F, 0.25F, 0.75F, 1F, 0.75F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
        if (world.isRemote)
            return true;

        TilePedestal ped = (TilePedestal)world.getTileEntity(x, y, z);
        ItemStack inv = ped.getStackInSlot(0);
        ItemStack equipped = player.getCurrentEquippedItem();

        if (inv != null) {
            if(equipped != null && inv.getItem() == equipped.getItem() && inv.getItemDamage() == equipped.getItemDamage()){
                if(inv.stackSize < 64) {
                    inv.stackSize += equipped.stackSize;
                    if(inv.stackSize > 64)
                        equipped.stackSize = inv.stackSize - 64;
                    else equipped.stackSize = 0;
                }

                ped.setInventorySlotContents(0, inv);
                player.setCurrentItemOrArmor(0, equipped);

                player.inventory.markDirty();
                world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                return true;
            }

            if(equipped != null && equipped.getItem() instanceof ItemWandCasting)
                return true;

            InventoryUtils.dropItemsAtEntity(world, x, y, z, player);
            world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
            return true;
        }

        if (equipped != null) {
            ped.setInventorySlotContents(0, equipped);
            player.setCurrentItemOrArmor(0, null);

            player.inventory.markDirty();

            world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);

            return true;
        }
        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister ir) {}

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isNormalCube() {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata){
        return new InfusionCoreTile();
    }

    @Override
    public boolean canStabaliseInfusion(World world, int x, int y, int z) {
        return true;
    }
}
