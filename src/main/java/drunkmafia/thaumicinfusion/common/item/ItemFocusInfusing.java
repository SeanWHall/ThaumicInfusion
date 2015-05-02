package drunkmafia.thaumicinfusion.common.item;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.Lux;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockJar;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.items.wands.foci.ItemFocusWarding;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileWarded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Sean on 04/04/2015.
 */
public class ItemFocusInfusing extends ItemFocusBasic {

    public IIcon iconOrnament;
    IIcon depthIcon = null;

    public ItemFocusInfusing(){
        this.setCreativeTab(ThaumicInfusion.instance.tab);
    }

    public String getSortingHelper(ItemStack itemstack) {
        return "BWI" + super.getSortingHelper(itemstack);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.depthIcon = ir.registerIcon("thaumcraft:focus_warding_depth");
        this.icon = ir.registerIcon("thaumcraft:focus_warding");
        this.iconOrnament = ir.registerIcon("thaumcraft:focus_warding_orn");
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

    public int getFocusColor(ItemStack itemstack) {
        NBTTagCompound wandNBT = itemstack.getTagCompound();
        if(wandNBT != null)
            return Aspect.getAspect(wandNBT.getString("InfusionAspect")).getColor();
        return 16771535;
    }

    public AspectList getVisCost(ItemStack itemstack) {
        return new AspectList();
    }

    public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, MovingObjectPosition mop) {
        player.swingItem();
        if(!world.isRemote) {
            if(mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                NBTTagCompound wandNBT = itemstack.getTagCompound() != null ? itemstack.getTagCompound() : new NBTTagCompound();
                TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
                if (tile != null && tile instanceof TileJarFillable) {
                    Aspect aspect = ((TileJarFillable)tile).getAspects().getAspects()[0];
                    if(aspect != null) {
                        wandNBT.setString("InfusionAspect", aspect.getTag());
                    }
                } else if(wandNBT.hasKey("InfusionAspect"))
                    placeAspect(player, new WorldCoord(mop.blockX, mop.blockY, mop.blockZ), Aspect.getAspect(wandNBT.getString("InfusionAspect")));

                itemstack.setTagCompound(wandNBT);
            }
        }
        return itemstack;
    }

    public void placeAspect(EntityPlayer player, WorldCoord pos, Aspect aspect){
        if(aspect != null) {
            TIWorldData worldData = TIWorldData.getWorldData(player.worldObj);
            WorldCoord coords = new WorldCoord(pos.x, pos.y, pos.z);
            if (player.isSneaking()) {
                worldData.removeData(BlockData.class, pos, true);
            } else {
                BlockData data = worldData.getBlock(BlockData.class, coords);
                if (data == null) {
                    Class c = AspectHandler.getEffectFromAspect(aspect);
                    if(c == null)
                        return;

                    worldData.addBlock(new BlockData(coords, new Class[]{c}), true, true);
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
                                worldData.addBlock(new BlockData(coords, newAspects.toArray(new Class[newAspects.size()])), true, true);
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
                        worldData.addBlock(new BlockData(coords, newAspects.toArray(new Class[newAspects.size()])), true, true);
                    }
                }
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(pos.x, pos.y, pos.z, 16556032), new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, (double) pos.x, (double) pos.y, (double) pos.z, 32.0D));
            }
        }
    }

    public boolean drainAspects(EntityPlayer player, Aspect aspect){
        if(player.capabilities.isCreativeMode)
            return true;

        ItemStack[] inventory = player.inventory.mainInventory;
        int cost = AspectHandler.getCostOfEffect(aspect);
        for(ItemStack stack : inventory){
            if(stack.getItem() instanceof IEssentiaContainerItem){
                AspectList aspects = ((IEssentiaContainerItem) stack.getItem()).getAspects(stack);
                if(aspects.getAmount(aspect) > cost){
                    aspects.reduce(aspect, cost);
                    ((IEssentiaContainerItem) stack.getItem()).setAspects(stack, aspects);
                    return true;
                }
            }
        }

        for(int x = (int) (player.posX - 10); x < player.posX + 10; x++){
            for(int y = (int) (player.posY - 10); y < player.posY + 10; y++){
                for(int z = (int) (player.posZ - 10); z < player.posZ + 10; z++){
                    TileEntity tileEntity = player.worldObj.getTileEntity(x, y, z);
                    if(tileEntity instanceof IAspectSource){
                        IAspectSource source = (IAspectSource) tileEntity;
                        AspectList aspects = source.getAspects();
                        if(aspects.getAmount(aspect) > cost){
                            aspects.reduce(aspect, cost);
                            source.setAspects(aspects);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
