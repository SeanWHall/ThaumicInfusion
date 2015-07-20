/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.util.helper.MathHelper;
import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ItemApi;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.ItemFocusBasic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class ClientEventContainer {

    private static int[] connectedTextureRefByID = new int[]{0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11};
    private static IIcon[] wardedGlassIcon;

    private static Class renderEventHandler;
    private static Object obj;
    private static Method drawTagsOnContainer;
    private static Field tagscale;
    private static HashMap<WorldCoordinates, IIcon> iconCache = new HashMap();

    static {
        try {
            renderEventHandler = Class.forName("thaumcraft.client.lib.RenderEventHandler");
            Class thaumcraftClass = Class.forName("thaumcraft.common.Thaumcraft");
            obj = thaumcraftClass.getDeclaredField("renderEventHandler").get(thaumcraftClass.getDeclaredField("instance").get(null));

            for (Method method : renderEventHandler.getDeclaredMethods())
                if (method.getName().equals("drawTagsOnContainer")) drawTagsOnContainer = method;
            tagscale = renderEventHandler.getDeclaredField("tagscale");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BlockData currentdata, lastDataLookedAt;

//    @SubscribeEvent
//    public void onDrawDebugText(RenderGameOverlayEvent.Text event) {
//        if(Minecraft.getMinecraft().gameSettings.showDebugInfo)
//            event.left.add("Detection time (Inaccurate) " + BlockHandler.blockHandlerTime + " ns");
//    }

    public static ItemFocusBasic getFocus(ItemStack stack) {
        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("focus")) {
            NBTTagCompound nbt = stack.stackTagCompound.getCompoundTag("focus");
            return (ItemFocusBasic) ItemStack.loadItemStackFromNBT(nbt).getItem();
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public void blockHighlight(DrawBlockHighlightEvent event) throws Exception {
        MovingObjectPosition target = event.target;
        EntityPlayer player = event.player;

        if (player.isSneaking() && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemApi.getItem("itemWandCasting", 0).getItem().getClass()) && getFocus(player.getCurrentEquippedItem()) != null && getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {
            if (lastDataLookedAt == null || lastDataLookedAt.getCoords().x != target.blockX || lastDataLookedAt.getCoords().y != target.blockY || lastDataLookedAt.getCoords().z != target.blockZ) {
                    TIWorldData worldData = TIWorldData.getWorldData(player.worldObj);
                    if (worldData != null)
                        lastDataLookedAt = worldData.getBlock(BlockData.class, new WorldCoordinates(target.blockX, target.blockY, target.blockZ, player.dimension));
            }

            if (lastDataLookedAt != null) {
                ForgeDirection dir = MathHelper.sideToDirection(target.sideHit);
                AspectList list = new AspectList();
                for (Aspect aspect : lastDataLookedAt.getAspects())
                    list.add(aspect, AspectHandler.getCostOfEffect(aspect));

                float scale = ((Float) tagscale.get(obj));
                if (scale < 0.5F)
                    tagscale.set(obj, scale + 0.031F - scale / 10.0F);

                drawTagsOnContainer.invoke(obj, (double) ((float) target.blockX + (float) dir.offsetX / 2.0F), (double) ((float) target.blockY + (float) dir.offsetY / 2.0F), (double) ((float) target.blockZ + (float) dir.offsetZ / 2.0F), list, 220, dir, event.partialTicks);
            }
        }
    }

    @SubscribeEvent
    public void renderLast(RenderWorldLastEvent event) throws Exception {

        float partialTicks = event.partialTicks;
        if (Minecraft.getMinecraft().renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().renderViewEntity;
            World world = player.worldObj;

            if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemApi.getItem("itemWandCasting", 0).getItem().getClass()) && getFocus(player.getCurrentEquippedItem()) != null && getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {
                TIWorldData worldData = TIWorldData.getWorldData(world);
                if (worldData == null)
                    return;
                for (ChunkData chunk : worldData.getChunksInRange((int) player.posX - 40, (int) player.posZ - 40, 80, 80)) {
                    if (chunk == null) continue;

                    for (BlockSavable savable : chunk.getAllBlocks()) {
                        if (savable == null || !(savable instanceof BlockData)) continue;

                        BlockData data = (BlockData) savable;
                        int x = data.getCoords().x, y = data.getCoords().y, z = data.getCoords().z;
                        currentdata = data;

                        double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
                        double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
                        double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 1);
                        GL11.glAlphaFunc(516, 0.003921569F);
                        GL11.glTranslated(-iPX + x + 0.5D, -iPY + y, -iPZ + z + 0.5D);

                        RenderBlocks renderBlocks = new RenderBlocks();
                        GL11.glDisable(2896);
                        Tessellator t = Tessellator.instance;
                        renderBlocks.setRenderBounds(-0.0010000000474974513D, -0.0010000000474974513D, -0.0010000000474974513D, 1.0010000467300415D, 1.0010000467300415D, 1.0010000467300415D);
                        Aspect[] aspects = data.getAspects();
                        if (aspects == null)
                            return;

                        new RGB(aspects[0].getColor()).glColor3f();

                        t.startDrawingQuads();
                        t.setBrightness(200);
                        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                        GL11.glTexEnvi(8960, 8704, 260);

                        Block blockJar = Block.getBlockFromItem(ItemApi.getBlock("blockJar", 0).getItem());

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[1], y - Facing.offsetsYForSide[1], z - Facing.offsetsZForSide[1]))
                            renderBlocks.renderFaceYNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 0, player.ticksExisted));

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[0], y - Facing.offsetsYForSide[0], z - Facing.offsetsZForSide[0]))
                            renderBlocks.renderFaceYPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 1, player.ticksExisted));

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[3], y - Facing.offsetsYForSide[3], z - Facing.offsetsZForSide[3]))
                            renderBlocks.renderFaceZNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 2, player.ticksExisted));

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[2], y - Facing.offsetsYForSide[2], z - Facing.offsetsZForSide[2]))
                            renderBlocks.renderFaceZPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 3, player.ticksExisted));

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[5], y - Facing.offsetsYForSide[5], z - Facing.offsetsZForSide[5]))
                            renderBlocks.renderFaceXNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 4, player.ticksExisted));

                        if (!isConnectedBlock(world, x - Facing.offsetsXForSide[4], y - Facing.offsetsYForSide[4], z - Facing.offsetsZForSide[4]))
                            renderBlocks.renderFaceXPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(world, x, y, z, 5, player.ticksExisted));

                        t.draw();
                        GL11.glTexEnvi(8960, 8704, 8448);
                        GL11.glEnable(2896);
                        GL11.glAlphaFunc(516, 0.1F);
                        GL11.glDisable(3042);
                        GL11.glColor3f(1.0F, 1.0F, 1.0F);
                        GL11.glPopMatrix();
                    }
                }
            }
        }
    }

    private boolean isConnectedBlock(World world, int x, int y, int z) {
        BlockData data = TIWorldData.getWorldData(world).getBlock(BlockData.class, new WorldCoordinates(x, y, z, world.provider.dimensionId));
        if (data == null)
            return false;

        int same = 0;
        for (Aspect aspect : data.getAspects()) {
            for (Aspect aspect2 : currentdata.getAspects()) {
                if (aspect == aspect2) {
                    same++;
                    break;
                }
            }
        }
        return same == data.getAspects().length;
    }

    private IIcon getIconOnSide(World world, int x, int y, int z, int side, int ticks) throws Exception {
        WorldCoordinates wc = new WorldCoordinates(x, y, z, side);
        IIcon out = iconCache.get(wc);
        if ((ticks + side) % 10 == 0 || out == null) {
            boolean[] bitMatrix = new boolean[8];
            if (side == 0 || side == 1) {
                bitMatrix[0] = this.isConnectedBlock(world, x - 1, y, z - 1);
                bitMatrix[1] = this.isConnectedBlock(world, x, y, z - 1);
                bitMatrix[2] = this.isConnectedBlock(world, x + 1, y, z - 1);
                bitMatrix[3] = this.isConnectedBlock(world, x - 1, y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + 1, y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x - 1, y, z + 1);
                bitMatrix[6] = this.isConnectedBlock(world, x, y, z + 1);
                bitMatrix[7] = this.isConnectedBlock(world, x + 1, y, z + 1);
            }

            if (side == 2 || side == 3) {
                bitMatrix[0] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y + 1, z);
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y + 1, z);
                bitMatrix[3] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y - 1, z);
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y - 1, z);
            }

            if (side == 4 || side == 5) {
                bitMatrix[0] = this.isConnectedBlock(world, x, y + 1, z + (side == 5 ? 1 : -1));
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x, y + 1, z + (side == 4 ? 1 : -1));
                bitMatrix[3] = this.isConnectedBlock(world, x, y, z + (side == 5 ? 1 : -1));
                bitMatrix[4] = this.isConnectedBlock(world, x, y, z + (side == 4 ? 1 : -1));
                bitMatrix[5] = this.isConnectedBlock(world, x, y - 1, z + (side == 5 ? 1 : -1));
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x, y - 1, z + (side == 4 ? 1 : -1));
            }

            int idBuilder = 0;

            for (int i = 0; i <= 7; ++i)
                idBuilder += bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4 : (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32 : (i == 6 ? 64 : 128))))))) : 0;


            if (wardedGlassIcon == null)
                wardedGlassIcon = (IIcon[]) Class.forName("thaumcraft.common.blocks.BlockCosmeticOpaque").getDeclaredField("wardedGlassIcon").get(null);

            out = (idBuilder <= 255 && idBuilder >= 0) ? wardedGlassIcon[connectedTextureRefByID[idBuilder]] : wardedGlassIcon[0];
            iconCache.put(wc, out);
        }

        return out;
    }

}
