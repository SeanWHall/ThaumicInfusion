package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
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
 * Created by DrunkMafia on 05/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("iter"), cost = 4)
public class Iter extends AspectEffect {

    public static long maxCooldown = 5000L;
    long startCooldown;

    WorldCoord destination;

    @Override
    public void updateBlock(World world) {
        if(world.isRemote)
            return;
        WorldCoord pos = getPos();

        if(pos == null || destination == null)
            return;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 2, pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for(EntityPlayer ent : ents)
            if(ent.isSneaking())
                teleportEntity(ent);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack wand = player.getCurrentEquippedItem();
        if (world.isRemote || wand == null)
            return wand != null && wand.getItem() instanceof ItemWandCasting;

        if (player.isSneaking()) {
            BlockData data = TIWorldData.getData(BlockData.class, getDestinationWorld(), destination);
            destination = null;
            if (data != null && data.hasEffect(Iter.class))
                data.getEffect(Iter.class).destination = null;
            return true;
        }

        NBTTagCompound compound = wand.stackTagCompound == null ? new NBTTagCompound() : wand.stackTagCompound;
        if (compound.hasKey("id") && compound.getString("id").equals("Iter")) {
            destination = new WorldCoord();
            destination.readNBT(compound);
            if (destination.equals(getPos())) {
                destination = null;
                return false;
            }
            destination.removeFromNBT(compound);
            destination.id = "Dest";
            wand.stackTagCompound = compound;

            BlockData data = TIWorldData.getData(BlockData.class, getDestinationWorld(), destination);
            if (data == null) {
                destination = null;
                return false;
            }

            Iter destIter = data.getEffect(Iter.class);
            if (destIter == null) {
                destination = null;
            } else {
                WorldCoord pos = getPos();
                pos.id = "Dest";
                pos.dim = world.provider.dimensionId;
                destIter.destination = pos;
            }

            return true;
        }

        WorldCoord pos = new WorldCoord("Iter", x, y, z);
        pos.dim = world.provider.dimensionId;
        pos.writeNBT(compound);
        wand.stackTagCompound = compound;
        return true;
    }

    World getDestinationWorld(){
        return destination != null ? DimensionManager.getWorld(destination.dim) : null;
    }

    void teleportEntity(Entity entity){
        if(entity.worldObj.isRemote || destination == null)
                return;

        if(safeToTeleport() && entity instanceof EntityLivingBase && startCooldown < System.currentTimeMillis()) {
            startCooldown = System.currentTimeMillis() + maxCooldown;
            if(TIWorldData.getData(BlockData.class, getDestinationWorld(), destination) == null){
                destination = null;
                return;
            }
            TIWorldData.getData(BlockData.class, getDestinationWorld(), destination).getEffect(Iter.class).startCooldown = startCooldown;
            if(destination.dim != entity.worldObj.provider.dimensionId)
                entity.travelToDimension(destination.dim);
            ((EntityLivingBase) entity).setPositionAndUpdate(destination.x + 0.5F, destination.y + 1F, destination.z + 0.5F);
        }
    }

    boolean safeToTeleport(){
        BlockData destData = TIWorldData.getData(BlockData.class, getDestinationWorld(), destination);
        if(destData == null || destData.getEffect(Iter.class) == null) {
            destination = null;
            return false;
        }
        return getDestinationWorld().isAirBlock(destination.x, destination.y + 1, destination.z) && getDestinationWorld().isAirBlock(destination.x, destination.y + 2, destination.z);
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
