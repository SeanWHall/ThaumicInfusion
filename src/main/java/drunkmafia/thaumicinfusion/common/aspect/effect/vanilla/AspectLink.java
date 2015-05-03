package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.items.wands.ItemWandCasting;

import java.util.ArrayList;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class AspectLink extends AspectEffect {
    WorldCoord destination;

    @OverrideBlock
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack wand = player.getCurrentEquippedItem();
        if(world.isRemote || wand == null)
            return wand != null && wand.getItem() instanceof ItemWandCasting;

        if(player.isSneaking()){
            BlockData data = TIWorldData.getData(BlockData.class, getDestinationWorld(), destination);
            destination = null;
            if(data != null && data.hasEffect(getClass()))
                data.getEffect(getClass()).destination = null;
            return true;
        }

        NBTTagCompound compound = wand.stackTagCompound == null ? new NBTTagCompound() : wand.stackTagCompound;
        if(compound.hasKey("id") && compound.getString("id").equals(getClass().getSimpleName())){
            destination = new WorldCoord();
            destination.readNBT(compound);
            if(destination.equals(getPos())) {
                destination = null;
                return false;
            }
            destination.removeFromNBT(compound);
            destination.id = "Dest";
            wand.stackTagCompound = compound;

            BlockData data = TIWorldData.getData(BlockData.class, getDestinationWorld(), destination);
            if(data == null){
                destination = null;
                System.out.println("Dest is null");
                return false;
            }

            AspectLink destIter = data.getEffect(getClass());
            if(destIter == null) {
                destination = null;
                System.out.println("NULL");
            }else {
                WorldCoord pos = getPos();
                pos.id = "Dest";
                pos.dim = world.provider.dimensionId;
                destIter.destination = pos;
                System.out.println("Set Destination Pos");
            }

            return true;
        }

        WorldCoord pos = new WorldCoord(getClass().getSimpleName(), x, y, z);
        pos.dim = world.provider.dimensionId;
        pos.writeNBT(compound);
        wand.stackTagCompound = compound;
        System.out.println("Saving NBT");
        return true;
    }

    public WorldCoord getDestination(){
        World world = getDestinationWorld();
        if(world == null || destination == null)
            return destination = null;

        BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, destination);
        if(blockData == null) return destination = null;

        AspectLink link = blockData.getEffect(getClass());
        return link.destination = getPos();
    }

    World getDestinationWorld(){
        return destination != null ? DimensionManager.getWorld(destination.dim) : null;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);

        if(destination == null)
            return;

        destination.writeNBT(tagCompound);
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);

        if(!tagCompound.hasKey("id"))
            return;

        destination = new WorldCoord();
        destination.readNBT(tagCompound);
    }
}
