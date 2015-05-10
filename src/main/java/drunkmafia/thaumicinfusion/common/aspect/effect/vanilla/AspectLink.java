package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.common.network.NetworkRegistry;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class AspectLink extends AspectEffect {

    private static Map<Integer, WorldCoord> positions = new HashMap<Integer, WorldCoord>();
    public WorldCoord destination;

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack wand = player.getCurrentEquippedItem();
        if (world.isRemote || wand == null || !(wand.getItem() instanceof ItemWandCasting))
            return;

        WorldCoord pos = getPos();
        pos.dim = world.provider.dimensionId;

        WorldCoord savedDestination = positions.get(wand.hashCode());
        if (savedDestination != null) {
            positions.remove(wand.hashCode());
            World desinationWorld = DimensionManager.getWorld(pos.dim);

            BlockData data = TIWorldData.getData(BlockData.class, desinationWorld, savedDestination);
            if (data == null) return;

            AspectLink link = data.getEffect(getClass());
            if (link == null || link == this) return;

            link.destination = pos;
            destination = savedDestination;
            positions.remove(wand.hashCode());

            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y, z, 16556032), new NetworkRegistry.TargetPoint(player.worldObj.provider.dimensionId, (double) x, (double) y, (double) z, 32.0D));
            ChannelHandler.network.sendToDimension(new BlockSyncPacketC(TIWorldData.getData(BlockData.class, world, pos)), world.provider.dimensionId);
            return;
        }

        positions.put(wand.hashCode(), pos);
        world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
    }

    public WorldCoord getDestination(){
        World world = getDestinationWorld();
        if(world == null || destination == null)
            return destination = null;

        BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, destination);
        if(blockData == null) return destination = null;

        return (blockData.getEffect(getClass()) != null ? destination : (destination = null));
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
