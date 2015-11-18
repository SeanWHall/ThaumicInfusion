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
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.internal.WorldCoordinates;

public class AspectLink extends AspectEffect {

    public WorldCoordinates destination;

    @BlockMethod(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, BlockPos blockPos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack paper = player.getCurrentEquippedItem();

        if (world.isRemote || paper == null || (paper.getItem() != TIItems.coordinatePaper && paper.getItem() != Items.paper))
            return false;

        NBTTagCompound paperTag = paper.getTagCompound();

        WorldCoordinates pos = new WorldCoordinates(blockPos, player.dimension);
        if (paper.getItem() == TIItems.coordinatePaper && paperTag != null && paperTag.hasKey("CoordinateX")) {
            WorldCoordinates storedDest = new WorldCoordinates(new BlockPos(paperTag.getInteger("CoordinateX"), paperTag.getInteger("CoordinateY"), paperTag.getInteger("CoordinateZ")), paperTag.getInteger("CoordinateDim"));
            World worldDest = DimensionManager.getWorld(storedDest.dim);

            BlockData data = TIWorldData.getWorldData(worldDest).getBlock(BlockData.class, storedDest);
            if (data == null || data.getEffect(getClass()) == null || data.getEffect(getClass()) == this) {
                player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.fail")));
                return false;
            }

            ((AspectLink) data.getEffect(getClass())).destination = pos;
            destination = storedDest;

            player.inventory.mainInventory[player.inventory.currentItem] = new ItemStack(Items.paper);

            player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.end")));
            world.playSoundEffect((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
            return false;
        }

        player.addChatMessage(new ChatComponentText(ThaumicInfusion.translate("ti.linking.begin")));

        if (paper.stackSize > 1) {
            paper.stackSize--;
            world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, paper));
        }

        paper = new ItemStack(TIItems.coordinatePaper);
        paperTag = paper.getTagCompound() != null ? paper.getTagCompound() : new NBTTagCompound();

        paperTag.setInteger("CoordinateX", blockPos.getX());
        paperTag.setInteger("CoordinateY", blockPos.getY());
        paperTag.setInteger("CoordinateZ", blockPos.getZ());
        paperTag.setInteger("CoordinateDim", player.dimension);

        paper.setTagCompound(paperTag);

        player.inventory.mainInventory[player.inventory.currentItem] = paper;

        world.playSoundEffect((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, "thaumcraft:zap", 0.25F, 1.0F);
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
        if (destination == null)
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
