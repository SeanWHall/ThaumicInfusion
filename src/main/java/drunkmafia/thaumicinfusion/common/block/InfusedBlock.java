package drunkmafia.thaumicinfusion.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import drunkmafia.thaumicinfusion.common.util.helper.InfusionHelper;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.client.DestroyBlockPacketS;
import drunkmafia.thaumicinfusion.net.packet.client.RequestBlockPacketS;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Logger;
import thaumcraft.common.items.wands.ItemWandCasting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InfusedBlock extends Block implements ITileEntityProvider {

    /**
     * =================================================
     * ===== Start Of Generic Block Functionality ======
     * =================================================
     */

    public static int renderType = -1;
    public boolean isOpaque = false;

    public InfusedBlock(Material mat) {
        super(mat);
        this.setTickRandomly(true);
        this.setStepSound(new SoundType("stone", -10, 1F));
    }

    public InfusedBlock setSlipperiness(float slipperiness) {
        this.slipperiness = slipperiness;
        return this;
    }

    public InfusedBlock setOpaque(boolean val){
        this.opaque = val;
        return this;
    }

    public boolean isBlockData(BlockSavable savable) {
        return savable != null && savable instanceof BlockData;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        blockIcon = iconRegister.registerIcon(BlockInfo.infusedBlock_BlankTexture);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int id) {}

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    protected void dropBlockAsItem(World world, int x, int y, int z, ItemStack p_149642_5_) {}

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, WorldCoord.get(x, y, z));

        if(isBlockData(data)){
            for(Block aspect : data.runAllAspectMethod())
                aspect.breakBlock(world, x, y, z, block, meta);

            if(!world.isRemote) {
                SoundType stepSound = data.getContainingBlock().stepSound;
                world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), stepSound.getBreakSound(), (stepSound.getVolume() + 1.0F) / 2.0F, stepSound.getPitch() * 0.8F);

                float f = 0.7F;
                double tempX = world.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + x;
                double tempY = world.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + y;
                double tempZ = world.rand.nextFloat() * f + (double) (1.0F - f) * 0.5D + z;

                EntityItem entityitem = new EntityItem(world, tempX, tempY, tempZ, InfusionHelper.getInfusedItemStack(data.getAspects(), new ItemStack(data.getContainingBlock()), 1, meta));
                entityitem.delayBeforeCanPickup = 10;
                world.spawnEntityInWorld(entityitem);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase ent, ItemStack stack) {
        if (world.isRemote)
            RequestBlockPacketS.syncTimeouts.remove(new WorldCoord(x, y, z));

        BlockData data = InfusionHelper.getDataFromStack(stack, world, x, y, z);
        if(data != null)
            TIWorldData.getWorldData(world).addBlock(data, true);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        WorldCoord pos = new WorldCoord(x, y, z);
        BlockData block = TIWorldData.getData(BlockData.class, world, pos);
        if(block == null) {
            world.setBlock(x, y, z, Blocks.air);
            return null;
        }
        return InfusionHelper.getInfusedItemStack(block.getAspects(), new ItemStack(block.getContainingBlock()), 1, world.getBlockMetadata(pos.x, pos.y, pos.z));
    }

    @Override
    public boolean canReplace(World world, int x, int y, int z, int side, ItemStack stack) {
        return true;
    }

    @Override
    public int getRenderType() {
        return renderType;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean isOpaqueCube() {
        return isOpaque;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
    public int tickRate(World world) {
        return 5;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(target.blockX, target.blockY, target.blockZ));
        if (isBlockData(blockData)) {
            try {
                {
                    Block block = blockData.getContainingBlock();
                    if (block.getMaterial() != Material.air) {
                        Random rand = new Random();

                        float space = 0.1F;
                        double x = target.blockX + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (space * 2.0F)) + (double) space + block.getBlockBoundsMinX();
                        double y = target.blockY + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double) (space * 2.0F)) + (double) space + block.getBlockBoundsMinY();
                        double z = target.blockZ + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (space * 2.0F)) + (double) space + block.getBlockBoundsMinZ();

                        if (target.sideHit == 0)
                            y = (double) target.blockY + block.getBlockBoundsMinY() - (double) space;
                        if (target.sideHit == 1)
                            y = target.blockY + block.getBlockBoundsMaxY() + (double) space;
                        if (target.sideHit == 2)
                            z = (double) target.blockZ + block.getBlockBoundsMinZ() - (double) space;
                        if (target.sideHit == 3)
                            z = target.blockZ + block.getBlockBoundsMaxZ() + (double) space;
                        if (target.sideHit == 4)
                            x = (double) target.blockX + block.getBlockBoundsMinX() - (double) space;
                        if (target.sideHit == 5)
                            x = target.blockX + block.getBlockBoundsMaxX() + (double) space;

                        effectRenderer.addEffect((new EntityDiggingFX(world, x, y, z, 0.0D, 0.0D, 0.0D, block, world.getBlockMetadata(target.blockX, target.blockY, target.blockZ))).applyColourMultiplier(target.blockX, target.blockY, target.blockZ).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
                    }
                }
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return true;
    }

    @Override
    public boolean func_149730_j(){
        return true;
    }

    /**
     * =================================================
     * ===== End Of Generic Block Functionality ========
     * =================================================
     * =================================================
     * ===== Start Of Infused Block Functionality ======
     * =================================================
     */

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().getSelectedBoundingBoxFromPool(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB bb, List list, Entity entity) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().addCollisionBoxesToList(world, x, y, z, bb, list, entity);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 pos, Vec3 dir) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().collisionRayTrace(world, x, y, z, pos, dir);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return super.collisionRayTrace(world, x, y, z, pos, dir);
    }

    @Override
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().getIcon(access, x, y, z, side);

        return blockIcon;
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onPostBlockPlaced(world, x, y, z, meta);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().updateTick(world, x, y, z, rand);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                effectRenderer.addBlockDestroyEffects(x, y, z, blockData.getContainingBlock(), meta);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            ItemStack stack = player.getHeldItem();
            if (world.isRemote && stack != null && stack.getItem() instanceof ItemWandCasting && blockData.canOpenGUI()) {
                player.openGui(ThaumicInfusion.instance, 0, world, x, y, z);
                return true;
            }
            try {
                Block block = blockData.runBlockMethod();
                System.out.println(block);
                return block.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().getBlocksMovement(access, x, y, z);

        return super.getBlocksMovement(access, x, y, z);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onBlockAdded(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                Block block = blockData.runBlockMethod();
                block.setBlockBoundsBasedOnState(access, x, y, z);

                this.setBlockBounds((float) block.getBlockBoundsMinX(), (float) block.getBlockBoundsMinY(), (float) block.getBlockBoundsMinZ(), (float) block.getBlockBoundsMaxX(), (float) block.getBlockBoundsMaxY(), (float) block.getBlockBoundsMaxZ());
            }catch (Exception e) {
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().randomDisplayTick(world, x, y, z, rand);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onPlantGrow(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onPlantGrow(world, x, y, z, sourceX, sourceY, sourceZ);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onNeighborBlockChange(world, x, y, z, block);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onFallenUpon(World world, int x, int y, int z, Entity ent, float fall) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onFallenUpon(world, x, y, z, ent, fall);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onEntityWalking(World world, int x, int y, int z, Entity ent) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onEntityWalking(world, x, y, z, ent);
                if(!world.isRemote) {
                    SoundType stepSound = blockData.getContainingBlock().stepSound;
                    world.playSoundEffect((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), stepSound.getStepResourcePath(), (stepSound.getVolume()) / 4.0F, stepSound.getPitch() * 0.8F);
                }
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity ent) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onEntityCollidedWithBlock(world, x, y, z, ent);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer ent) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                blockData.runBlockMethod().onBlockClicked(world, x, y, z, ent);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int meta) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isProvidingWeakPower(access, x, y, z, meta);

        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess access, int x, int y, int z, int meta) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isProvidingStrongPower(access, x, y, z, meta);
        return 0;
    }

    @Override
    public int getLightValue(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                Block effect = blockData.runAspectMethod();
                return effect != null ? effect.getLightValue(access, x, y, z) : 0;
            }catch (Exception e){
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }
        return 0;
    }

    @Override
    public int getLightOpacity(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getLightOpacity(access, x, y, z);
            }catch (Exception e){
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }
        return getLightOpacity();
    }

    @Override
    public int getFlammability(IBlockAccess access, int x, int y, int z, ForgeDirection face) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getFlammability(access, x, y, z, face);
            }catch (Exception e){
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess access, int x, int y, int z, ForgeDirection face) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getFireSpreadSpeed(access, x, y, z, face);
            }catch (Exception e){
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }

        return 0;
    }

    @Override
    public ForgeDirection[] getValidRotations(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().getValidRotations(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return null;
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getPlayerRelativeBlockHardness(player, world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getComparatorInputOverride(world, x, y, z, side);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB bb = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().getCollisionBoundingBoxFromPool(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return bb;
    }

    @Override
    public float getExplosionResistance(Entity ent, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().getExplosionResistance(ent, world, x, y, z, explosionX, explosionY, explosionZ);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().getBlockHardness(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess access, int x, int y, int z, int side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().shouldCheckWeakPower(access, x, y, z, side);
        return false;
    }

    @Override
    public float getEnchantPowerBonus(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().getEnchantPowerBonus(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
        return !(access.getBlock(x, y, z) instanceof InfusedBlock);
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().rotateBlock(world, x, y, z, axis);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().recolourBlock(world, x, y, z, side, colour);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public int colorMultiplier(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().colorMultiplier(access, x, y, z);
            } catch (Exception e) {
                handleError(e, TIWorldData.getWorld(access), blockData, true);
            }
        }
        return 0;
    }

    @Override
    public boolean isWood(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().isWood(access, x, y, z);
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess access, int x, int y, int z, ForgeDirection side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().isSideSolid(access, x, y, z, side);
        return true;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess access, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isBeaconBase(access, x, y, z, beaconX, beaconY, beaconZ);
        return false;
    }

    @Override
    public boolean isBed(IBlockAccess access, int x, int y, int z, EntityLivingBase player) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isBed(access, x, y, z, player);
        return false;
    }

    @Override
    public boolean isBedFoot(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isBedFoot(access, x, y, z);
        return false;
    }

    @Override
    public int getBedDirection(IBlockAccess world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(world), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().getBedDirection(world, x, y, z);
        return 0;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess access, int x, int y, int z, int meta) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().isBlockSolid(access, x, y, z, meta);
        return true;
    }

    @Override
    public boolean isBurning(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isBurning(access, x, y, z);
        return false;
    }

    @Override
    public boolean isFertile(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().isFertile(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.runBlockMethod().isFireSource(world, x, y, z, side);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public boolean isFlammable(IBlockAccess access, int x, int y, int z, ForgeDirection face) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isFlammable(access, x, y, z, face);
        return false;
    }

    @Override
    public boolean isFoliage(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isFoliage(access, x, y, z);
        return false;
    }

    @Override
    public boolean isLadder(IBlockAccess access, int x, int y, int z, EntityLivingBase entity) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isLadder(access, x, y, z, entity);
        return false;
    }

    @Override
    public boolean isLeaves(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().isLeaves(access, x, y, z);
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().isNormalCube(access, x, y, z);
        return false;
    }

    @Override
    public boolean isReplaceable(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().isReplaceable(access, x, y, z);
        return false;
    }

    @Override
    public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().canPlaceTorchOnTop(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockAccess access, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().canSustainPlant(access, x, y, z, direction, plantable);
        return false;
    }

    @Override
    public boolean canSustainLeaves(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().canSustainLeaves(access, x, y, z);
        return false;
    }

    @Override
    public boolean getWeakChanges(IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().getWeakChanges(access, x, y, z);
        return false;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess access, int x, int y, int z, int side) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().canConnectRedstone(access, x, y, z, side);
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess access, int x, int y, int z, Entity entity) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.getContainingBlock().canEntityDestroy(access, x, y, z, entity);
        return false;
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess access, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, TIWorldData.getWorld(access), new WorldCoord(x, y, z));
        if (isBlockData(blockData))
            return blockData.runBlockMethod().canCreatureSpawn(type, access, x, y, z);
        return false;
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
        if (isBlockData(blockData)) {
            try {
                return blockData.getContainingBlock().canBlockStay(world, x, y, z);
            } catch (Exception e) {
                handleError(e, world, blockData, true);
            }
        }
        return false;
    }

    /**
     * ===============================================
     * ===== End Of Infused Block Functionality ======
     * ===============================================
     */

    /**
     * ==================================================
     * ===== Start Of Infused Block Error Handling ======
     * ==================================================
     */

    /**
     * Used by the generated tile entities
     *
     * @param e Exception thrown
     * @param entity Tile entity that caused the exception
     */
    public static void handleError(Exception e, TileEntity entity){
        handleError(e, entity.getWorldObj(), TIWorldData.getData(BlockData.class, entity.getWorldObj(), WorldCoord.get(entity.xCoord, entity.yCoord, entity.zCoord)), true);
    }

    /**
     * Handles all errors thrown by block, tile entity or event
     *
     * @param e Exception thrown
     * @param world The World it occured in
     * @param data The data of the block
     * @param shouldDestroy Will destroy the block if true
     */
    public static void handleError(Exception e, World world, BlockData data, boolean shouldDestroy) {
        String methName = Thread.currentThread().getStackTrace()[2].getMethodName();
        Logger logger = ThaumicInfusion.getLogger();
        logger.error("Block at: " + data.getCoords().toString() + " threw error while running: " + methName + " in block: " + data.getContainingBlock().getLocalizedName() + " it is advised that this block is added to the blacklist in the config.", e);

        if (shouldDestroy) {
            logger.info("Block has been destroyed, to prevent this error from happening again");
            if(!world.isRemote)
                TIWorldData.getWorldData(world).removeBlock(data.getCoords(), true);
            else
                ChannelHandler.network.sendToServer(new DestroyBlockPacketS(data.getCoords()));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return null;
    }

    /**
     * ================================================
     * ===== End Of Infused Block Error Handling ======
     * ================================================
     */
}