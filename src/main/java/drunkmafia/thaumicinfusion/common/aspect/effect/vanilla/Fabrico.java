/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.lang.reflect.Field;

@Effect(aspect = "fabrico", cost = 4)
public class Fabrico extends AspectEffect {

    private static Field blockHardnessField;

    public Block block;

    @Override
    @BlockMethod(overrideBlockFunc = false)
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        ItemStack itemInHand = player.getCurrentEquippedItem();

        if (!world.isRemote && itemInHand != null && itemInHand.getItem() instanceof ItemBlock) {
            world.playSoundEffect((double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);

            block = Block.getBlockFromItem(itemInHand.getItem());
            if (block == world.getBlockState(pos).getBlock()) block = null;

            ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
        }
    }

    @Override
    @BlockMethod
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return (block != null ? block : world.getBlockState(pos).getBlock()).getExplosionResistance(exploder);
    }

    @Override
    @BlockMethod
    public float getBlockHardness(World world, BlockPos pos) {
        float ret = 0;

        try {
            if (blockHardnessField == null) {
                blockHardnessField = Block.class.getDeclaredField("blockHardness");
                blockHardnessField.setAccessible(true);
            }

            ret = (Float) blockHardnessField.get((block != null ? block : world.getBlockState(pos).getBlock()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.hasKey("block"))
            block = Block.getBlockById(tagCompound.getInteger("block"));
        else
            block = null;
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if (block != null)
            tagCompound.setInteger("block", Block.getIdFromBlock(block));
    }
}
