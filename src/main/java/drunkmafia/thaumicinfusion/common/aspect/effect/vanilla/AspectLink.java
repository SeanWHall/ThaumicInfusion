package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.items.wands.ItemWandCasting;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class AspectLink extends AspectEffect {
    ;

    private static Map<Integer, WorldCoord> positions = new HashMap<Integer, WorldCoord>();
    public WorldCoord destination;

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

        WorldCoord savedDestination = positions.get(wand.hashCode());
        if (savedDestination != null) {
            positions.remove(wand.hashCode());

            BlockData data = TIWorldData.getData(BlockData.class, world, savedDestination);
            if (data == null) return true;
            AspectLink link = data.getEffect(getClass());
            if (link == null || link == this) return true;
            link.destination = getPos();
            destination = savedDestination;
            positions.remove(wand.hashCode());
            return true;
        }

        positions.put(wand.hashCode(), getPos());
        return true;
    }

    public WorldCoord getDestination(){
        World world = getDestinationWorld();
        if(world == null || destination == null)
            return destination = null;

        BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, destination);
        if(blockData == null) return destination = null;

        AspectLink link = blockData.getEffect(getClass());
        link.destination = getPos();
        return destination;
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
