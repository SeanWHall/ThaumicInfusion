package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.tileentity.PermutatioTile;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 08/10/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("permutatio"), cost = 4, hasTileEntity = true)
public class Permutatio  extends AspectEffect {

    @Override
    public TileEntity getTile() {
        return new PermutatioTile();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if(world.isRemote)
            return data != null ? data.getContainingBlock().onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ) : true;

        ItemStack wand = player.getCurrentEquippedItem();
        if(wand == null)
            return data.getContainingBlock().onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);

        PermutatioTile tile;
        if(world.getTileEntity(x, y, z) != null)
            tile = (PermutatioTile) world.getTileEntity(x, y, z);
        else
            return data.getContainingBlock().onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
        if(player.isSneaking()){
            tile.linkedPos = null;
            return true;
        }

        NBTTagCompound compound = wand.stackTagCompound == null ? new NBTTagCompound() : wand.stackTagCompound;
        if(compound.hasKey("id") && compound.getString("id").equals("Perm")){
            WorldCoord destination = WorldCoord.get(compound);
            destination.removeFromNBT(compound);
            wand.stackTagCompound = compound;

            TileEntity linkingTile = world.getTileEntity(destination.x, destination.y, destination.z);
            if(linkingTile != null && linkingTile instanceof IInventory)
                tile.linkedPos = destination;

            world.setTileEntity(x, y, z, tile);
            return true;
        }
        return data.getContainingBlock().onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void worldBlockInteracted(EntityPlayer player, World world, int x, int y, int z, int face) {
        if(world.isRemote)
            return;

        WorldCoord pos = getPos();
        if(x == pos.x && y == pos.y && z == pos.z)
            return;

        TileEntity tile = world.getTileEntity(x, y, z);
        ItemStack wand = player.getCurrentEquippedItem();
        if(wand == null || tile == null || !(tile instanceof IInventory))
            return;

        NBTTagCompound compound = wand.stackTagCompound == null ? new NBTTagCompound() : wand.stackTagCompound;

        if(compound.hasKey("id") && compound.getString("id").equals("Perm"))
            pos.removeFromNBT(compound);

        pos = new WorldCoord("Perm", x, y, z);
        pos.writeNBT(compound);
        wand.stackTagCompound = compound;
    }
}
