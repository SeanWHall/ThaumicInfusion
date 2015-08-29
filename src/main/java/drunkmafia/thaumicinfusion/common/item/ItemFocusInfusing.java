/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.wands.ItemFocusBasic;

import java.util.ArrayList;
import java.util.List;

public class ItemFocusInfusing extends ItemFocusBasic {

    private static List<Block> blockblacklist = new ArrayList<Block>();

    static{
        String[] blocks = ThaumicInfusion.instance.config.get("Block Blacklist", "Blocks that are banned from being infused", new String[] {"bedrock"}).getStringList();
        for(String block : blocks) blockblacklist.add(Block.getBlockFromName(block));
    }

    public IIcon iconOrnament, depthIcon = null;

    public ItemFocusInfusing(){
        this.setCreativeTab(ThaumicInfusion.instance.tab);
    }

    public String getSortingHelper(ItemStack itemstack) {
        return "BWI" + super.getSortingHelper(itemstack);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.depthIcon = ir.registerIcon(ModInfo.MODID + ":focus_infusion_depth");
        this.icon = ir.registerIcon(ModInfo.MODID + ":focus_infusion");
        this.iconOrnament = ir.registerIcon(ModInfo.MODID + ":focus_infusion_orn");
    }

    public IIcon getFocusDepthLayerIcon(ItemStack itemstack) {
        return this.depthIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int par1, int renderPass) {
        return renderPass == 1?this.icon:this.iconOrnament;
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    public IIcon getOrnament(ItemStack itemstack) {
        return this.iconOrnament;
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return new AspectList();
    }

    public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, MovingObjectPosition mop) {
        player.swingItem();
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if(blockblacklist.contains(world.getBlock(mop.blockX, mop.blockY, mop.blockZ))) return itemstack;

            NBTTagCompound wandNBT = itemstack.getTagCompound() != null ? itemstack.getTagCompound() : new NBTTagCompound();

            if (wandNBT.hasKey("InfusionAspect") && !world.isRemote) {
                Aspect aspect = Aspect.getAspect(wandNBT.getString("InfusionAspect"));
                if (aspect != null) {
                    placeAspect(player, new WorldCoordinates(mop.blockX, mop.blockY, mop.blockZ, player.dimension), aspect);
                    world.playSoundEffect((double) mop.blockX + 0.5D, (double) mop.blockY + 0.5D, (double) mop.blockZ + 0.5D, "thaumcraft:wand", 0.7F, world.rand.nextFloat() * 0.1F + 0.9F);
                }
            }
        } else {
            player.openGui(ThaumicInfusion.instance, 0, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }

        return itemstack;
    }

    public void placeAspect(EntityPlayer player, WorldCoordinates pos, Aspect aspect) {
        if(aspect != null) {
            World world = player.worldObj;
            TIWorldData worldData = TIWorldData.getWorldData(world);
            WorldCoordinates coords = new WorldCoordinates(pos.x, pos.y, pos.z, player.dimension);
            if (player.isSneaking()) {
                BlockData data = worldData.getBlock(BlockData.class, pos);
                if (data != null) {
                    AspectList list = new AspectList();
                    for (Aspect currentAspect : data.getAspects())
                        list.add(currentAspect, AspectHandler.getCostOfEffect(aspect));
                    refillJars(player, list);

                    worldData.removeData(BlockData.class, pos, true);
                }
            } else {
                BlockData data = worldData.getBlock(BlockData.class, coords);
                if (data == null) {
                    Class c = AspectHandler.getEffectFromAspect(aspect);
                    if(c == null)
                        return;
                    if (drainAspects(player, aspect))
                        data = new BlockData(coords, new Class[]{c});
                }else{
                    for(Aspect dataAspect : data.getAspects()){
                        if(dataAspect == aspect){
                            ArrayList<Class> newAspects = new ArrayList<Class>();
                            for(Aspect dataAspect2 : data.getAspects()){
                                if(dataAspect2 != aspect)
                                    newAspects.add(AspectHandler.getEffectFromAspect(dataAspect2));
                            }

                            if(newAspects.size() == 0)
                                worldData.removeData(BlockData.class, pos, true);
                            else if(drainAspects(player, aspect)){
                                worldData.removeData(BlockData.class, pos, true);
                                data = new BlockData(coords, newAspects.toArray(new Class[newAspects.size()]));
                                for (AspectEffect effect : data.getEffects())
                                    effect.onPlaceEffect(player);
                                worldData.addBlock(data, true, true);
                            }
                            return;
                        }
                    }
                    if(drainAspects(player, aspect)) {
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
    }

    public boolean drainAspects(EntityPlayer player, Aspect aspect){
        if(player.capabilities.isCreativeMode)
            return true;

        int cost = AspectHandler.getCostOfEffect(aspect);
        for(int x = (int) (player.posX - 10); x < player.posX + 10; x++){
            for(int y = (int) (player.posY - 10); y < player.posY + 10; y++){
                for(int z = (int) (player.posZ - 10); z < player.posZ + 10; z++){
                    TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
                    if(tileEntity instanceof IAspectSource){
                        IAspectSource source = (IAspectSource) tileEntity;
                        if (source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            player.worldObj.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
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
                        TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
                        if (tileEntity instanceof IAspectSource) {
                            IAspectSource source = (IAspectSource) tileEntity;
                            if (source.doesContainerAccept(currentAspect)) {
                                source.addToContainer(currentAspect, AspectHandler.getCostOfEffect(currentAspect));
                                filled++;
                                foundJar = true;
                                player.worldObj.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.3F, false);
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
