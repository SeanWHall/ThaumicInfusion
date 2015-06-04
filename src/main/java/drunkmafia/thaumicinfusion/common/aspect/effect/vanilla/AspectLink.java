/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

/*
 * Created by DrunkMafia on 25/07/2014.
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.common.network.NetworkRegistry;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;

import java.util.HashMap;
import java.util.Map;

public class AspectLink extends AspectEffect {

    private static Map<Integer, WorldCoordinates> positions = new HashMap<>();
    public WorldCoordinates destination;

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack wand = player.getCurrentEquippedItem();
        if (world.isRemote || wand == null || !(wand.getItem() instanceof ItemWandCasting))
            return false;

        WorldCoordinates pos = new WorldCoordinates(x, y, z, player.dimension);
        if (positions.containsKey(wand.hashCode())) {
            WorldCoordinates storedDest = positions.get(wand.hashCode());
            World worldDest = DimensionManager.getWorld(storedDest.dim);

            BlockData data = TIWorldData.getWorldData(worldDest).getBlock(BlockData.class, storedDest);
            AspectLink linkDest;
            if (data == null || (linkDest = data.getEffect(getClass())) == null || linkDest == this) return false;

            linkDest.destination = pos;
            destination = storedDest;

            positions.remove(wand.hashCode());

            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(x, y, z, 16556032), new NetworkRegistry.TargetPoint(pos.dim, (double) x, (double) y, (double) z, 32.0D));
            return false;
        }

        positions.put(wand.hashCode(), pos);
        world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
        return false;
    }

    public WorldCoordinates getDestination() {
        World world;
        if (destination == null || (world = DimensionManager.getWorld(destination.dim)) == null)
            return destination = null;

        BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, destination);
        return (blockData != null && blockData.hasEffect(getClass())) ? destination : (destination = null);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        super.writeNBT(nbt);

        if(destination == null)
            return;
        destination.writeNBT(nbt);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        super.readNBT(nbt);

        if (!nbt.hasKey("dest_x"))
            destination = null;
        destination = new WorldCoordinates();
        destination.readNBT(nbt);
    }
}
