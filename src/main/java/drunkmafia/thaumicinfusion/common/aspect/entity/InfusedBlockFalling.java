package drunkmafia.thaumicinfusion.common.aspect.entity;

import drunkmafia.thaumicinfusion.common.util.helper.InfusionHelper;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class InfusedBlockFalling extends Entity {

    private BlockData data;
    private int meta, blocks;

    public InfusedBlockFalling(World world){
        super(world);
    }

    public InfusedBlockFalling(World world, double x, double y, double z, BlockData data, int meta)
    {
        super(world);
        this.data = data;
        this.meta = meta;
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
        ++this.blocks;
        this.motionY -= 0.03999999910593033D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if(data == null)
            setDead();

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

                    if (this.worldObj.canPlaceEntityOnSide(data.getContainingBlock(), x, y, z, true, 1, null, null) && !BlockFalling.func_149831_e(this.worldObj, x, y - 1, z)) {
                        data.setCoords(new WorldCoord(x, y, z));
                        TIWorldData.getWorldData(worldObj).addBlock(data, true);
                        ChannelHandler.network.sendToDimension(new BlockSyncPacketC(data), worldObj.provider.dimensionId);
                        worldObj.setBlock(x, y, z, data.getBlock(), meta, 3);

                    } else {
                        dropAsItem(x, y, z);
                    }
                }
            } else if (this.blocks > 100 && !this.worldObj.isRemote && (y < 1 || y > 256) || this.blocks > 600) {
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

        EntityItem entityitem = new EntityItem(worldObj, tempX, tempY, tempZ, InfusionHelper.getInfusedItemStack(data.getAspects(), new ItemStack(data.getContainingBlock()), 1, meta));
        entityitem.delayBeforeCanPickup = 10;
        worldObj.spawnEntityInWorld(entityitem);
    }


    protected void writeEntityToNBT(NBTTagCompound nbt){
        if(data == null)
            return;
        nbt.setTag("dataNBT", SavableHelper.saveDataToNBT(data));
        nbt.setInteger("blockMETA", meta);
    }

    protected void readEntityFromNBT(NBTTagCompound nbt) {
        if(!nbt.hasKey("dataNBT"))
            return;
        data = SavableHelper.loadDataFromNBT(nbt.getCompoundTag("dataNBT"));
        meta = nbt.getInteger("blockMETA");
    }

    public BlockData getBlockData(){
        return this.data;
    }

    public int getMetaData(){
        return this.meta;
    }
}
