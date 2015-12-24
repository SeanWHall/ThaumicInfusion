/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.item;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.api.wands.ItemFocusBasic;

import java.util.ArrayList;
import java.util.List;

public class ItemFocusInfusing extends ItemFocusBasic {

    private static final List<Block> blockblacklist = new ArrayList<Block>();

    static {
        String[] blocks = ThaumicInfusion.instance.config.get("Block Blacklist", "Blocks that are banned from being infused", new String[]{"bedrock"}).getStringList();
        for (String block : blocks) ItemFocusInfusing.blockblacklist.add(Block.getBlockFromName(block));
    }

    public ItemFocusInfusing() {
        super("infusion", 8747923);
        setCreativeTab(ThaumicInfusion.instance.tab);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return new AspectList().add(Aspect.AIR, 0);
    }

    @Override
    public boolean onFocusActivation(ItemStack itemstack, World world, EntityLivingBase player, MovingObjectPosition mop, int count) {
        player.swingItem();

        if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
            if (ItemFocusInfusing.blockblacklist.contains(world.getBlockState(mop.getBlockPos()).getBlock()))
                return false;

            NBTTagCompound wandNBT = itemstack.getTagCompound() != null ? itemstack.getTagCompound() : new NBTTagCompound();

            if (!world.isRemote) {
                Aspect aspect = wandNBT.hasKey("InfusionAspect") ? Aspect.getAspect(wandNBT.getString("InfusionAspect")) : null;
                this.placeAspect((EntityPlayer) player, new WorldCoordinates(mop.getBlockPos(), player.dimension), aspect);
                world.playSoundEffect((double) mop.getBlockPos().getX() + 0.5D, (double) mop.getBlockPos().getY() + 0.5D, (double) mop.getBlockPos().getZ() + 0.5D, "thaumcraft:wand", 0.7F, world.rand.nextFloat() * 0.1F + 0.9F);
            }
        } else {
            ((EntityPlayer) player).openGui(ThaumicInfusion.instance, 0, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }

        return false;
    }

    public void placeAspect(EntityPlayer player, WorldCoordinates pos, Aspect aspect) {
        World world = player.worldObj;
        TIWorldData worldData = TIWorldData.getWorldData(world);
        WorldCoordinates coords = new WorldCoordinates(pos.pos, player.dimension);
        if (aspect == null) {
            BlockData data = worldData.getBlock(BlockData.class, pos);
            if (data != null) {
                AspectList list = new AspectList();
                for (Aspect currentAspect : data.getAspects())
                    list.add(currentAspect, AspectHandler.getCostOfEffect(currentAspect));
                this.refillJars(player, list);

                worldData.removeData(BlockData.class, pos, true);
            }
        } else {
            BlockData data = worldData.getBlock(BlockData.class, coords);
            if (data == null) {
                Class c = AspectHandler.getEffectFromAspect(aspect);
                if (c == null)
                    return;
                if (this.drainAspects(player, aspect))
                    data = new BlockData(coords, new Class[]{c});
            } else {
                for (Aspect dataAspect : data.getAspects()) {
                    if (dataAspect == aspect) {
                        ArrayList<Class> newAspects = new ArrayList<Class>();
                        for (Aspect dataAspect2 : data.getAspects()) {
                            if (dataAspect2 != aspect)
                                newAspects.add(AspectHandler.getEffectFromAspect(dataAspect2));
                        }

                        if (newAspects.size() == 0)
                            worldData.removeData(BlockData.class, pos, true);
                        else if (this.drainAspects(player, aspect)) {
                            worldData.removeData(BlockData.class, pos, true);
                            data = new BlockData(coords, newAspects.toArray(new Class[newAspects.size()]));
                            for (AspectEffect effect : data.getEffects())
                                effect.onPlaceEffect(player);
                            worldData.addBlock(data, true, true);
                        }
                        return;
                    }
                }
                if (this.drainAspects(player, aspect)) {
                    ArrayList<Class> newAspects = new ArrayList<Class>();
                    newAspects.add(AspectHandler.getEffectFromAspect(aspect));
                    for (Aspect dataAspect : data.getAspects())
                        newAspects.add(AspectHandler.getEffectFromAspect(dataAspect));

                    worldData.removeData(BlockData.class, pos, true);
                    data = new BlockData(coords, newAspects.toArray(new Class[newAspects.size()]));
                }
            }
            if (data != null) {
                for (AspectEffect effect : data.getEffects())
                    effect.onPlaceEffect(player);
                worldData.addBlock(data, true, true);
            }
        }

    }

    public boolean drainAspects(EntityPlayer player, Aspect aspect) {
        if (player.capabilities.isCreativeMode)
            return true;

        int cost = AspectHandler.getCostOfEffect(aspect);
        for (int x = (int) (player.posX - 10); x < player.posX + 10; x++) {
            for (int y = (int) (player.posY - 10); y < player.posY + 10; y++) {
                for (int z = (int) (player.posZ - 10); z < player.posZ + 10; z++) {
                    TileEntity tileEntity = player.worldObj.getTileEntity(new BlockPos(x, y, z));
                    if (tileEntity instanceof IAspectSource) {
                        IAspectSource source = (IAspectSource) tileEntity;
                        if (source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            player.worldObj.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean refillJars(EntityPlayer player, AspectList aspectList) {
        if (player.capabilities.isCreativeMode)
            return true;

        int filled = 0;
        for (int i = 0; i < aspectList.size(); i++) {
            boolean foundJar = false;
            Aspect currentAspect = aspectList.getAspects()[i];
            for (int x = (int) (player.posX - 10); x < player.posX + 10; x++) {
                for (int y = (int) (player.posY - 10); y < player.posY + 10; y++) {
                    for (int z = (int) (player.posZ - 10); z < player.posZ + 10; z++) {
                        TileEntity tileEntity = player.worldObj.getTileEntity(new BlockPos(x, y, z));
                        if (tileEntity instanceof IAspectSource) {
                            IAspectSource source = (IAspectSource) tileEntity;
                            if (source.doesContainerAccept(currentAspect)) {
                                source.addToContainer(currentAspect, AspectHandler.getCostOfEffect(currentAspect));
                                filled++;
                                foundJar = true;
                                player.worldObj.playSound((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
                                break;
                            }
                        }
                    }
                    if (foundJar) break;
                }
                if (foundJar) break;
            }
        }

        return filled == aspectList.size();
    }
}
