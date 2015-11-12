/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class InfusedBlockFalling extends Entity {

    public TileEntity tileEntity;
    public int meta, blockCount, id;

    public InfusedBlockFalling(World world) {
        super(world);
    }

    public InfusedBlockFalling(World world, double x, double y, double z, int id, int meta, TileEntity tileEntity) {
        super(world);
        this.id = id;
        this.meta = meta;
        this.tileEntity = tileEntity;
        preventEntitySpawning = true;
        setSize(0.98F, 0.98F);
        yOffset = height / 2.0F;
        setPosition(x, y, z);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = x;
        prevPosY = z;
        prevPosZ = y;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
    }

    public boolean canBeCollidedWith() {
        return !isDead;
    }

    public void onUpdate() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        ++blockCount;
        motionY -= 0.03999999910593033D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;

        if (!worldObj.isRemote) {
            int x = MathHelper.floor_double(posX);
            int y = MathHelper.floor_double(posY);
            int z = MathHelper.floor_double(posZ);

            if (onGround) {
                motionX *= 0.699999988079071D;
                motionZ *= 0.699999988079071D;
                motionY *= -0.5D;

                if (worldObj.getBlock(x, y, z) != Blocks.piston_extension) {
                    setDead();

                    if (worldObj.canPlaceEntityOnSide(Block.getBlockById(this.id), x, y, z, true, 1, null, null) && !BlockFalling.func_149831_e(worldObj, x, y - 1, z)) {
                        this.worldObj.setBlock(x, y, z, Block.getBlockById(this.id), this.meta, 3);
                        if (this.tileEntity != null) {
                            this.tileEntity.xCoord = x;
                            this.tileEntity.yCoord = y;
                            this.tileEntity.zCoord = z;
                            this.tileEntity.setWorldObj(this.worldObj);

                            if (this.worldObj.getTileEntity(x, y, z) != null) {
                                NBTTagCompound tileTag = new NBTTagCompound();
                                this.tileEntity.writeToNBT(tileTag);
                                this.worldObj.getTileEntity(x, y, z).readFromNBT(tileTag);
                            } else {
                                this.worldObj.setTileEntity(x, y, z, this.tileEntity);
                            }
                        }
                    } else
                        this.dropAsItem(x, y, z);
                }
            } else if (blockCount > 100 && !worldObj.isRemote && (y < 1 || y > 256) || blockCount > 600) {
                this.dropAsItem(x, y, z);
                setDead();
            }
        }
    }

    public void dropAsItem(int x, int y, int z) {
        float f = 0.7F;
        double tempX = this.worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + x;
        double tempY = this.worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + y;
        double tempZ = this.worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + z;

        EntityItem entityitem = new EntityItem(this.worldObj, tempX, tempY, tempZ, new ItemStack(Block.getBlockById(this.id), 1, this.meta));

        entityitem.delayBeforeCanPickup = 10;
        this.worldObj.spawnEntityInWorld(entityitem);
    }


    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("blockID", this.id);
        nbt.setInteger("blockMETA", this.meta);

        if (this.tileEntity != null) {
            NBTTagCompound tileTag = new NBTTagCompound();
            this.tileEntity.writeToNBT(tileTag);
            nbt.setTag("tileTAG", tileTag);
        }
    }

    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.id = nbt.getInteger("blockID");
        this.meta = nbt.getInteger("blockMETA");

        if (nbt.hasKey("tileTAG"))
            this.tileEntity = TileEntity.createAndLoadEntity(nbt.getCompoundTag("tileTAG"));
    }

    public int getMetaData() {
        return meta;
    }
}
