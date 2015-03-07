package drunkmafia.thaumicinfusion.common.item;

import drunkmafia.thaumicinfusion.common.block.IWorldData;
import drunkmafia.thaumicinfusion.common.util.helper.InfusionHelper;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ItemInfused extends Item {

    public ItemInfused() {
        setUnlocalizedName("item.infused");
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int id = InfusionHelper.getInfusedID(stack);

        try {
            return "Infused " + (id != -1 ? (new ItemStack(Block.getBlockById(id), 1, stack.getItemDamage())).getDisplayName() : "");
        }catch (Exception e){
            return "NULL STACK, PLEASE DESTROY IT";
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        BlockData data = InfusionHelper.getDataFromStack(stack, world, x, y, z);

        if(data == null)
            return false;

        ItemStack containingStack = stack.copy();
        containingStack.func_150996_a(Item.getItemFromBlock(data.getContainingBlock()));

        if(containingStack.getItem().onItemUse(containingStack, player, world, x, y, z, side, hitX, hitY, hitZ)) {
            Block block = world.getBlock(x, y, z);
            if(block == Blocks.snow_layer) {
                side = 1;
            } else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z)) {
                if (side == 0)
                    --y;

                if (side == 1)
                    ++y;

                if (side == 2)
                    --z;

                if (side == 3)
                    ++z;

                if (side == 4)
                    --x;

                if (side == 5)
                    ++x;
            }

            stack.stackSize--;

            Block worldBlock = world.getBlock(x, y, z);
            data = InfusionHelper.getDataFromStack(stack, world, x, y, z);
            data.setContainingBlock(worldBlock);
            TIWorldData.getWorldData(world).addBlock(data, true);
            return true;
        }
        return false;
    }
}
