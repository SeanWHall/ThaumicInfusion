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

    public InfusedBlockFalling(World world){
        super(world);
    }

    public InfusedBlockFalling(World world, double x, double y, double z, int id, int meta, TileEntity tileEntity)
    {
        super(world);
        this.id = id;
        this.meta = meta;
        this.tileEntity = tileEntity;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = z;
        this.prevPosZ = y;
    }

    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit() {}

    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.blockCount;
        this.motionY -= 0.03999999910593033D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (!this.worldObj.isRemote)
        {
            int x = MathHelper.floor_double(this.posX);
            int y = MathHelper.floor_double(this.posY);
            int z = MathHelper.floor_double(this.posZ);

            if (this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
                this.motionY *= -0.5D;

                if (this.worldObj.getBlock(x, y, z) != Blocks.piston_extension)
                {
                    this.setDead();

                    if (this.worldObj.canPlaceEntityOnSide(Block.getBlockById(id), x, y, z, true, 1, null, null) && !BlockFalling.func_149831_e(this.worldObj, x, y - 1, z)) {
                        worldObj.setBlock(x, y, z, Block.getBlockById(id), meta, 3);
                        if (tileEntity != null) {
                            tileEntity.xCoord = x;
                            tileEntity.yCoord = y;
                            tileEntity.zCoord = z;
                            tileEntity.setWorldObj(worldObj);

                            if (worldObj.getTileEntity(x, y, z) != null) {
                                NBTTagCompound tileTag = new NBTTagCompound();
                                tileEntity.writeToNBT(tileTag);
                                worldObj.getTileEntity(x, y, z).readFromNBT(tileTag);
                            } else {
                                worldObj.setTileEntity(x, y, z, tileEntity);
                            }
                        }
                    } else
                        dropAsItem(x, y, z);
                }
            } else if (this.blockCount > 100 && !this.worldObj.isRemote && (y < 1 || y > 256) || this.blockCount > 600) {
                dropAsItem(x, y, z);
                this.setDead();
            }
        }
    }

    public void dropAsItem(int x, int y, int z){
        float f = 0.7F;
        double tempX = worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + x;
        double tempY = worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + y;
        double tempZ = worldObj.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + z;

        EntityItem entityitem = new EntityItem(worldObj, tempX, tempY, tempZ, new ItemStack(Block.getBlockById(id), 1, meta));

        entityitem.delayBeforeCanPickup = 10;
        worldObj.spawnEntityInWorld(entityitem);
    }


    protected void writeEntityToNBT(NBTTagCompound nbt){
        nbt.setInteger("blockID", id);
        nbt.setInteger("blockMETA", meta);

        if (tileEntity != null) {
            NBTTagCompound tileTag = new NBTTagCompound();
            tileEntity.writeToNBT(tileTag);
            nbt.setTag("tileTAG", tileTag);
        }
    }

    protected void readEntityFromNBT(NBTTagCompound nbt) {
        id = nbt.getInteger("blockID");
        meta = nbt.getInteger("blockMETA");

        if (nbt.hasKey("tileTAG"))
            tileEntity = TileEntity.createAndLoadEntity(nbt.getCompoundTag("tileTAG"));
    }

    public int getMetaData(){
        return this.meta;
    }
}
