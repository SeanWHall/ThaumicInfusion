package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.BlockWrapper;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@Effect(aspect = "instrumentum", cost = 4)
public class Instrumentum extends AspectEffect {

    private String owner;
    private boolean locked = true;
    private List<String> children = new ArrayList<String>();

    @Override
    public void onPlaceEffect(EntityPlayer player) {
        owner = player.getName();
    }

    @Override
    @BlockMethod
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (!worldIn.isRemote && playerIn.isSneaking()) {
            if (playerIn.getName().equals(owner)) {
                locked = !locked;
                playerIn.addChatMessage(new ChatComponentText("Block " + (locked ? "Locked" : "Unlocked")));
                ChannelHandler.instance().sendToDimension(new EffectSyncPacketC(this, false), worldIn.provider.getDimensionId());
            } else if (!locked) {
                if (children.contains(playerIn.getName())) {
                    playerIn.addChatMessage(new ChatComponentText("Unauthorized"));
                    children.remove(playerIn.getName());
                } else {
                    playerIn.addChatMessage(new ChatComponentText("Authorized"));
                    children.add(playerIn.getName());
                }
                ChannelHandler.instance().sendToDimension(new EffectSyncPacketC(this, false), worldIn.provider.getDimensionId());
            }
        }

        if (isAllowedAccess(playerIn.getName())) {
            BlockWrapper.suppressed = true;
            worldIn.getBlockState(pos).getBlock().onBlockClicked(worldIn, pos, playerIn);
        }
    }

    private boolean isAllowedAccess(String name) {
        if (owner.equals(name)) return true;
        for (String child : children) if (child.equals(name)) return true;
        return false;
    }

    @Override
    @BlockMethod
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (isAllowedAccess(playerIn.getName())) {
            BlockWrapper.suppressed = true;
            return worldIn.getBlockState(pos).getBlock().onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        owner = tagCompound.getString("owner");
        int size = tagCompound.getInteger("size_Children");
        for (int i = 0; i < size; i++) children.add(tagCompound.getString(i + "_Children"));
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setString("owner", owner);
        tagCompound.setInteger("size_Children", children.size());
        for (int i = 0; i < children.size(); i++) tagCompound.setString(i + "_Children", children.get(i));
    }
}
