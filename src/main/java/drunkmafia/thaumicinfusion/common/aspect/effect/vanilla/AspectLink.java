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
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;

public abstract class AspectLink extends AspectEffect {

    public WorldCoordinates destination;

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        ItemStack paper = player.getCurrentEquippedItem();
        if (world.isRemote || paper == null || paper.getItem() != TIItems.coordinatePaper && paper.getItem() != Items.paper)
            return false;

        NBTTagCompound paperTag = paper.getTagCompound();

        WorldCoordinates pos = new WorldCoordinates(x, y, z, player.dimension);
        if (paper.getItem() == TIItems.coordinatePaper && paperTag != null && paperTag.hasKey("CoordinateX")) {
            WorldCoordinates storedDest = new WorldCoordinates(paperTag.getInteger("CoordinateX"), paperTag.getInteger("CoordinateY"), paperTag.getInteger("CoordinateZ"), paperTag.getInteger("CoordinateDim"));
            World worldDest = DimensionManager.getWorld(storedDest.dim);

            BlockData data = TIWorldData.getWorldData(worldDest).getBlock(BlockData.class, storedDest);
            if (data == null || data.getEffect(this.getClass()) == null || data.getEffect(this.getClass()) == this) {
                player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.fail")));
                return false;
            }

            ((AspectLink) data.getEffect(this.getClass())).destination = pos;
            this.destination = storedDest;

            player.inventory.mainInventory[player.inventory.currentItem] = new ItemStack(Items.paper);

            player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.end")));
            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
            return false;
        }

        player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.begin")));

        if (paper.stackSize > 1) {
            paper.stackSize--;
            world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, paper));
        }

        paper = new ItemStack(TIItems.coordinatePaper);
        paperTag = paper.getTagCompound() != null ? paper.getTagCompound() : new NBTTagCompound();

        paperTag.setInteger("CoordinateX", x);
        paperTag.setInteger("CoordinateY", y);
        paperTag.setInteger("CoordinateZ", z);
        paperTag.setInteger("CoordinateDim", player.dimension);

        paper.setTagCompound(paperTag);

        player.inventory.mainInventory[player.inventory.currentItem] = paper;

        world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
        return false;
    }

    public WorldCoordinates getDestination() {
        World world;
        if (this.destination == null || (world = DimensionManager.getWorld(this.destination.dim)) == null)
            return this.destination = null;

        BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, this.destination);
        return blockData != null && blockData.hasEffect(getClass()) ? this.destination : (this.destination = null);
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        super.writeNBT(nbt);
        if (this.destination == null)
            return;
        this.destination.writeNBT(nbt);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        super.readNBT(nbt);

        if (!nbt.hasKey("dest_x"))
            this.destination = null;
        this.destination = new WorldCoordinates();
        this.destination.readNBT(nbt);
    }
}
