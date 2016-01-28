/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.event;

import drunkmafia.thaumicinfusion.client.world.IClientRenderer;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.BlockWrapper;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.lib.RenderEventHandler;

@SideOnly(Side.CLIENT)
public class ClientEventContainer {

    private BlockData lastDataLookedAt;
    private long lastUpdate, time;

    public static ItemFocusBasic getFocus(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("focus")) {
            NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("focus");
            return (ItemFocusBasic) ItemStack.loadItemStackFromNBT(nbt).getItem();
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public void onDrawDebugText(RenderGameOverlayEvent.Text event) {
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            if (System.currentTimeMillis() > (lastUpdate + 1000)) {
                time = BlockWrapper.handlerTime;
                lastUpdate = System.currentTimeMillis();
            }

            event.left.add("TI| Block Overhead: (NS) " + (time < 10000 ? (time < 2000 ? "§a " : time < 3000 ? "§e " : "§4 ") + time : "ERROR! Block Detection Taking too long! §4" + time));
        }
    }

    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) throws Exception {
        float partialTicks = event.partialTicks;

        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();
            World world = player.worldObj;

            TIWorldData worldData = TIWorldData.getWorldData(world);
            if (worldData == null)
                return;

            for (ChunkData chunk : worldData.getChunksInRange((int) player.posX - 64, (int) player.posZ - 64, (int) player.posX + 64, (int) player.posZ + 64)) {
                if (chunk == null) continue;

                for (BlockSavable savable : chunk.getAllBlocks()) {
                    if (savable != null && savable instanceof IClientRenderer)
                        ((IClientRenderer) savable).renderData(player, partialTicks);
                }
            }

            MovingObjectPosition target = Minecraft.getMinecraft().objectMouseOver;

            if (target == null || target.getBlockPos() == null) return;

            if (player.isSneaking() && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemsTC.wand.getClass()) && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) != null && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {
                if (this.lastDataLookedAt == null || this.lastDataLookedAt.getCoords().pos.getX() != target.getBlockPos().getX() || this.lastDataLookedAt.getCoords().pos.getY() != target.getBlockPos().getY() || this.lastDataLookedAt.getCoords().pos.getZ() != target.getBlockPos().getZ()) {
                    this.lastDataLookedAt = worldData.getBlock(BlockData.class, new WorldCoordinates(target.getBlockPos(), player.dimension));
                }

                if (this.lastDataLookedAt != null) {
                    EnumFacing dir = target.sideHit;
                    AspectList list = new AspectList();
                    for (Aspect aspect : this.lastDataLookedAt.getAspects())
                        list.add(aspect, AspectHandler.getCostOfEffect(aspect));

                    float scale = RenderEventHandler.tagscale;
                    if (scale < 0.5F)
                        RenderEventHandler.tagscale = scale + 0.031F - scale / 10.0F;
                    RenderEventHandler.drawTagsOnContainer((double) ((float) target.getBlockPos().getX() + (float) dir.getFrontOffsetX() / 2.0F), (double) ((float) target.getBlockPos().getY() + (float) dir.getFrontOffsetY() / 2.0F), (double) ((float) target.getBlockPos().getZ() + (float) dir.getFrontOffsetZ() / 2.0F), list, 220, dir, event.partialTicks);
                }
            }
        }
    }
}
