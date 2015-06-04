/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.block.tile;

import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;

public class InscriberTile extends TileEntity implements IInventory {

    public long spinStart;

    public Aspect aspect;
    public float topRotation = 0, hover, spin;

    private ItemStack[] inventory = new ItemStack[getSizeInventory()];

    public int getSizeInventory() {
        return 1;
    }

    public ItemStack getStackInSlot(int par1) {
        return this.inventory[par1];
    }

    public ItemStack decrStackSize(int par1, int par2) {
        if (this.inventory[par1] != null) {
            if (!this.worldObj.isRemote) {
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }

            ItemStack itemstack;
            if (this.inventory[par1].stackSize <= par2) {
                itemstack = this.inventory[par1];
                this.inventory[par1] = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.inventory[par1].splitStack(par2);
                if (this.inventory[par1].stackSize == 0) {
                    this.inventory[par1] = null;
                }

                this.markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int par1) {
        if (this.inventory[par1] != null) {
            ItemStack itemstack = this.inventory[par1];
            this.inventory[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
        this.inventory[par1] = par2ItemStack;
        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit()) {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
        if (!this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }

    }

    public String getInventoryName() {
        return BlockInfo.inscriberBlock_TileEntity;
    }

    public boolean hasCustomInventoryName() {
        return false;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("Aspect"))
            aspect = Aspect.getAspect(nbt.getString("Aspect"));
        else aspect = null;
        for (int i = 0; i < getSizeInventory(); i++) {
            if (nbt.hasKey("Item: " + i))
                inventory[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Item: " + i));
            else
                inventory[i] = null;
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (aspect != null)
            nbt.setString("Aspect", aspect.getTag());
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = inventory[i];
            if (stack == null) continue;

            NBTTagCompound stackTag = new NBTTagCompound();
            stack.writeToNBT(stackTag);
            nbt.setTag("Item: " + i, stackTag);
        }
    }

    public int getInventoryStackLimit() {
        return 1;
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, -999, nbttagcompound);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        this.readFromNBT(pkt.func_148857_g());
    }

    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && par1EntityPlayer.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
    }

    public void openInventory() {
    }

    public void closeInventory() {
    }

    public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
        return true;
    }

}
