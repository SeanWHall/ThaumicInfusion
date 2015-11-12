package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

@Effect(aspect = "sensus")
public class Sensus extends AspectEffect {

    private Block disguisedBlock;
    private int metadata;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            ChannelHandler.instance().sendToDimension(new EffectSyncPacketC(this, true), world.provider.dimensionId);
    }

    @Override
    public int getCost() {
        return 4;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) return;

        ItemStack stackInHand = player.getCurrentEquippedItem();

        if (player.isSneaking()) {
            this.disguisedBlock = null;
            ChannelHandler.instance().sendToDimension(new EffectSyncPacketC(this, true), world.provider.dimensionId);
        } else if (stackInHand != null && stackInHand.getItem() instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(stackInHand.getItem());
            if (block.isNormalCube() && block.renderAsNormalBlock()) {
                this.disguisedBlock = block;
                this.metadata = stackInHand.getItemDamage();
                ChannelHandler.instance().sendToDimension(new EffectSyncPacketC(this, true), world.provider.dimensionId);
            }
        }
    }

    @OverrideBlock
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        IIcon icon = this.disguisedBlock != null ? this.disguisedBlock.getIcon(side, this.metadata) : null;
        return icon != null ? icon : access.getBlock(x, y, z).getIcon(side, access.getBlockMetadata(x, y, z));
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.hasKey("disguisedBlock")) {
            this.disguisedBlock = Block.getBlockById(tagCompound.getInteger("disguisedBlock"));
            this.metadata = tagCompound.getInteger("metadata");
        } else this.disguisedBlock = null;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if (this.disguisedBlock != null) {
            tagCompound.setInteger("disguisedBlock", Block.getIdFromBlock(this.disguisedBlock));
            tagCompound.setInteger("metadata", this.metadata);
        }
    }
}
