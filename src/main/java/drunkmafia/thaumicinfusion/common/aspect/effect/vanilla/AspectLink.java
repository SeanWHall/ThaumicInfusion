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

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.ItemApi;
import thaumcraft.api.WorldCoordinates;

import java.util.HashMap;
import java.util.Map;

public class AspectLink extends AspectEffect {

    private static Map<Integer, WorldCoordinates> positions = new HashMap<Integer, WorldCoordinates>();
    public WorldCoordinates destination;

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack wand = player.getCurrentEquippedItem();
        if (world.isRemote || wand == null || !(wand.getItem().getClass().isAssignableFrom(ItemApi.getItem("itemWandCasting", 0).getItem().getClass())))
            return false;

        WorldCoordinates pos = new WorldCoordinates(x, y, z, player.dimension);
        if (positions.containsKey(wand.hashCode())) {
            WorldCoordinates storedDest = positions.get(wand.hashCode());
            World worldDest = DimensionManager.getWorld(storedDest.dim);

            BlockData data = TIWorldData.getWorldData(worldDest).getBlock(BlockData.class, storedDest);
            AspectLink linkDest;
            if (data == null || (linkDest = data.getEffect(getClass())) == null || linkDest == this){
                player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.fail")));
                return false;
            }

            linkDest.destination = pos;
            destination = storedDest;

            positions.remove(wand.hashCode());

            player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.end")));
            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
            return false;
        }

        player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.begin")));
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
