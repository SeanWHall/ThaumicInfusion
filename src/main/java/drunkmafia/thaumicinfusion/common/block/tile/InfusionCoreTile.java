package drunkmafia.thaumicinfusion.common.block.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.wands.IWandable;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileInfusionMatrix;
import thaumcraft.common.tiles.TilePedestal;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by DrunkMafia on 19/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class InfusionCoreTile extends TilePedestal implements IWandable {

    public static ArrayList<ItemStack> infuseStacksTemp = new ArrayList<ItemStack>();

    public TileInfusionMatrix matrix;

    @SideOnly(Side.CLIENT)
    public float angle;
    @SideOnly(Side.CLIENT)
    public Vector3 coreAxies;

    boolean shouldCheck;

    @Override
    public void updateEntity() {
        getMatrixTile();
        if(matrix != null) {
            if(!worldObj.isRemote) {
                if (matrix.crafting && shouldCheck)
                    shouldCheck = checkInfusion();
                else shouldCheck = true;
            }else
                particles();
        }
    }

    @SideOnly(Side.CLIENT)
    void particles(){
        if(matrix.crafting)
            Thaumcraft.proxy.burst(worldObj, xCoord + 0.5F, yCoord + 0.75F, zCoord + 0.5F, 0.5F);
    }

    boolean checkInfusion(){
        try {
            Field recipeOutput = TileInfusionMatrix.class.getDeclaredField("recipeOutput");
            Field recipeInput = TileInfusionMatrix.class.getDeclaredField("recipeInput");

            recipeOutput.setAccessible(true);
            recipeInput.setAccessible(true);

            Object output = recipeOutput.get(matrix);
            Object input = recipeInput.get(matrix);

            if(output == null || input == null)
                return false;

            if(Block.getBlockFromItem(((ItemStack)output).getItem()) instanceof InfusedBlock)
                return false;

            if(((ItemStack)input).stackSize > 1){
                ItemStack drop = ((ItemStack)input).copy();
                drop.stackSize -= 1;
                ((ItemStack)input).stackSize = 1;

                EntityItem item = new EntityItem(worldObj, xCoord, yCoord, zCoord, drop);
                worldObj.spawnEntityInWorld(item);

                ((ItemStack)output).stackSize = 1;
                recipeOutput.set(matrix, output);
                recipeInput.set(matrix, input);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    void getMatrixTile(){
        TileEntity matrixTile = worldObj.getTileEntity(xCoord, yCoord + 2, zCoord);
        if(matrixTile != null && matrixTile instanceof TileInfusionMatrix)
            matrix = (TileInfusionMatrix)matrixTile;
        else
            matrix = null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md){
        if ((!world.isRemote) && (matrix.active) && (!matrix.crafting)){
            ItemStack stack = getStackInSlot(0);
            if(stack != null) {
                infuseStacksTemp.add(stack);
                matrix.craftingStart(player);
                infuseStacksTemp.remove(stack);
                return 0;
            }
        }
        return -1;
    }

    @Override
    public ItemStack onWandRightClick(World world, ItemStack itemStack, EntityPlayer player) {return null;}
    @Override
    public void onUsingWandTick(ItemStack itemStack, EntityPlayer player, int i) {}
    @Override
    public void onWandStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int i) {}
}
