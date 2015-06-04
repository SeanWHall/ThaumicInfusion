/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.block.tile.InscriberTile;
import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.items.ItemEssence;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.util.Arrays;

public class InscriberBlock extends Block implements ITileEntityProvider {
    protected InscriberBlock() {
        super(Material.rock);
        setBlockName(BlockInfo.inscriberBlock_UnlocalizedName);
        setCreativeTab(ThaumicInfusion.instance.tab);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        blockIcon = iconRegister.registerIcon("thaumcraft:arcane_stone");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
        if (world.isRemote)
            return true;

        InscriberTile ped = (InscriberTile) world.getTileEntity(x, y, z);
        ItemStack inv = ped.getStackInSlot(0);
        ItemStack equipped = player.getCurrentEquippedItem();

        if (equipped != null && equipped.getItem() instanceof ItemEssence) {
            IEssentiaContainerItem containerItem = (IEssentiaContainerItem) equipped.getItem();
            int meta = equipped.getItemDamage();
            if (meta == 0 && ped.aspect != null) {
                AspectList aspectList = new AspectList();
                aspectList.add(ped.aspect, 8);
                if (equipped.stackSize > 1)
                    equipped.stackSize--;
                else
                    player.inventory.setInventorySlotContents(Arrays.asList(player.inventory.mainInventory).indexOf(equipped), null);

                ItemStack newPhial = new ItemStack(equipped.getItem());
                newPhial.setItemDamage(1);
                containerItem.setAspects(newPhial, aspectList);
                player.entityDropItem(newPhial, 0);
                ped.aspect = null;
            } else if (meta == 1) {
                Aspect aspect = containerItem.getAspects(equipped).getAspects()[0];
                if (aspect != ped.aspect) {
                    ped.aspect = aspect;
                    player.entityDropItem(new ItemStack(equipped.getItem(), 1, 0), 0);

                    if (equipped.stackSize > 1)
                        equipped.stackSize--;
                    else
                        player.inventory.setInventorySlotContents(Arrays.asList(player.inventory.mainInventory).indexOf(equipped), null);
                }
            }

            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(ped.getDescriptionPacket());
            return true;
        }

        if (inv != null) {
            if (equipped != null && inv.getItem() == equipped.getItem() && inv.getItemDamage() == equipped.getItemDamage()) {
                if (inv.stackSize < 64) {
                    inv.stackSize += equipped.stackSize;
                    if (inv.stackSize > 64)
                        equipped.stackSize = inv.stackSize - 64;
                    else equipped.stackSize = 0;
                }

                ped.setInventorySlotContents(0, inv);
                player.setCurrentItemOrArmor(0, equipped);

                player.inventory.markDirty();
                world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                return true;
            }

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
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        InventoryUtils.dropItems(world, x, y, z);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null && tileEntity instanceof InscriberTile)
            return ((InscriberTile) tileEntity).aspect != null ? 12 : 0;
        return 0;
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
    public TileEntity createNewTileEntity(World world, int meta) {
        return new InscriberTile();
    }
}
