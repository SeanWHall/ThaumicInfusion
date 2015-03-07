package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import thaumcraft.common.lib.utils.InventoryUtils;

/**
 * Created by DrunkMafia on 06/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("vacuos"), cost = 4, hasTileEntity = true)
public class Vacuos extends AspectEffect {

    @Override
    public TileEntity getTile(){
        return new VacuosTile();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        System.out.println(x + " " + y + " " + z);
        if (tileEntity == null || !(tileEntity instanceof IInventory))
            return false;

        player.displayGUIChest((IInventory) tileEntity);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if(!world.isRemote)
            InventoryUtils.dropItems(world, x, y, z);
    }

    public static class VacuosTile extends TileEntityChest {

        private ItemStack[] chestContents = new ItemStack[36];

        /**
         * Returns the number of slots in the inventory.
         */
        public int getSizeInventory() {
            return 27;
        }

        /**
         * Returns the stack in slot i
         */
        public ItemStack getStackInSlot(int p_70301_1_) {
            return this.chestContents[p_70301_1_];
        }

        /**
         * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
         * new stack.
         */
        public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
            if (this.chestContents[p_70298_1_] != null) {
                ItemStack itemstack;

                if (this.chestContents[p_70298_1_].stackSize <= p_70298_2_) {
                    itemstack = this.chestContents[p_70298_1_];
                    this.chestContents[p_70298_1_] = null;
                    this.markDirty();
                    return itemstack;
                } else {
                    itemstack = this.chestContents[p_70298_1_].splitStack(p_70298_2_);

                    if (this.chestContents[p_70298_1_].stackSize == 0) {
                        this.chestContents[p_70298_1_] = null;
                    }

                    this.markDirty();
                    return itemstack;
                }
            } else {
                return null;
            }
        }

        /**
         * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
         * like when you close a workbench GUI.
         */
        public ItemStack getStackInSlotOnClosing(int p_70304_1_) {
            if (this.chestContents[p_70304_1_] != null) {
                ItemStack itemstack = this.chestContents[p_70304_1_];
                this.chestContents[p_70304_1_] = null;
                return itemstack;
            } else {
                return null;
            }
        }

        /**
         * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
         */
        public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
            this.chestContents[p_70299_1_] = p_70299_2_;

            if (p_70299_2_ != null && p_70299_2_.stackSize > this.getInventoryStackLimit()) {
                p_70299_2_.stackSize = this.getInventoryStackLimit();
            }

            this.markDirty();
        }

        /**
         * Returns the name of the inventory
         */
        public String getInventoryName() {
            return "container.chest";
        }

        /**
         * Returns if the inventory is named
         */
        public boolean hasCustomInventoryName() {
            return false;
        }

        public void readFromNBT(NBTTagCompound p_145839_1_) {
            super.readFromNBT(p_145839_1_);
            NBTTagList nbttaglist = p_145839_1_.getTagList("Items", 10);
            this.chestContents = new ItemStack[this.getSizeInventory()];

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j >= 0 && j < this.chestContents.length) {
                    this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                }
            }
        }

        public void writeToNBT(NBTTagCompound p_145841_1_) {
            super.writeToNBT(p_145841_1_);
            NBTTagList nbttaglist = new NBTTagList();

            for (int i = 0; i < this.chestContents.length; ++i) {
                if (this.chestContents[i] != null) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("Slot", (byte) i);
                    this.chestContents[i].writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }

            p_145841_1_.setTag("Items", nbttaglist);
        }

        /**
         * Returns the maximum stack size for a inventory slot.
         */
        public int getInventoryStackLimit() {
            return 64;
        }

        /**
         * Do not make give this method the name canInteractWith because it clashes with Container
         */
        public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
            return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : p_70300_1_.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
        }


        public void openInventory() {
        }

        public void closeInventory() {
        }

        /**
         * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
         */
        public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
            return true;
        }
    }
}
