package drunkmafia.thaumicinfusion.common.aspect.tileentity;

import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class PermutatioTile extends EffectTile implements IInventory {

    public WorldCoord linkedPos;

    @Override
    public int getSizeInventory() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return 0;
        else
            return linkedTile.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return null;
        else
            return linkedTile.getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int i1) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return null;
        else
            return linkedTile.decrStackSize(i, i1);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return null;
        else
            return linkedTile.getStackInSlotOnClosing(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemStack) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile != null)
            linkedTile.setInventorySlotContents(i, itemStack);
    }

    @Override
    public String getInventoryName() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return null;
        else
            return linkedTile.getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return false;
        else
            return linkedTile.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return 0;
        else
            return linkedTile.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return false;
        else
            return linkedTile.isUseableByPlayer(entityPlayer);
    }

    @Override
    public void openInventory() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile != null)
            linkedTile.openInventory();
    }

    @Override
    public void closeInventory() {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile != null)
            linkedTile.closeInventory();
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        IInventory linkedTile = getLinkedTile();
        if(linkedTile == null)
            return false;
        else
            return linkedTile.isItemValidForSlot(i, itemStack);
    }

    public IInventory getLinkedTile(){
        if(linkedPos == null)
            return null;

        TileEntity tileEntity = worldObj.getTileEntity(linkedPos.x, linkedPos.y, linkedPos.z);
        if(tileEntity != null && tileEntity instanceof IInventory)
            return (IInventory)tileEntity;
        return null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if(linkedPos != null)
            linkedPos.writeNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if(nbt.hasKey("id"))
            linkedPos = WorldCoord.get(nbt);
        else
            linkedPos = null;
    }
}
