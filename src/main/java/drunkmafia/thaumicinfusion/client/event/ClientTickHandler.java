package drunkmafia.thaumicinfusion.client.event;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.helper.InfusionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;

/**
 * Created by DrunkMafia on 07/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 * <p/>
 * NOTICE: The following class contains code taken from TC
 * it has been ripped from TC with the permission of Azanor
 * and is used to make my infused block compatible with
 * seeing the aspect in the block
 */
@SideOnly(Side.CLIENT)
public class ClientTickHandler {
    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        if (event.phase != TickEvent.Phase.START) {
            if ((mc.renderViewEntity instanceof EntityPlayer)) {
                EntityPlayer player = (EntityPlayer) mc.renderViewEntity;

                GuiScreen gui = mc.currentScreen;
                if (((gui instanceof GuiContainer)) && (((GuiScreen.isShiftKeyDown()) && (!Config.showTags)) || ((!GuiScreen.isShiftKeyDown()) && (Config.showTags) && (!Mouse.isGrabbed()))))
                    renderAspectsInGui((GuiContainer) gui, player);
            }
        }
    }

    public void renderAspectsInGui(GuiContainer gui, EntityPlayer player) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        ScaledResolution var13 = new ScaledResolution(Minecraft.getMinecraft(), mc.displayWidth, mc.displayHeight);
        int var14 = var13.getScaledWidth();
        int var15 = var13.getScaledHeight();
        int var16 = Mouse.getX() * var14 / mc.displayWidth;
        int var17 = var15 - Mouse.getY() * var15 / mc.displayHeight - 1;


        GL11.glPushMatrix();
        GL11.glPushAttrib(1048575);
        GL11.glDisable(2896);
        for (int var20 = 0; var20 < gui.inventorySlots.inventorySlots.size(); var20++) {
            int xs = UtilsFX.getGuiXSize(gui);
            int ys = UtilsFX.getGuiYSize(gui);
            int shift = 0;
            int shift2 = 0;
            int shiftx = -8;
            int shifty = -8;
            if (Thaumcraft.instance.aspectShift) {
                shiftx -= 8;
                shifty -= 8;
            }
            Slot var23 = (Slot) gui.inventorySlots.inventorySlots.get(var20);
            int guiLeft = shift + (gui.width - xs - shift2) / 2;
            int guiTop = (gui.height - ys) / 2;
            if (isMouseOverSlot(var23, var16, var17, guiLeft, guiTop)) {
                if (var23.getStack() != null) {
                    ItemStack stack = var23.getStack();
                    AspectList infusedTag = InfusionHelper.getInfusedAspects(stack);
                    if (infusedTag != null) {
                        AspectList tags = InfusionHelper.addBlockAspects(stack);
                        if (tags != null) {
                            int x, y;
                            GL11.glDisable(2929);

                            int index = 0;
                            if (infusedTag.size() > 0) {
                                for (Aspect tag : infusedTag.getAspectsSortedAmount()) {
                                    if (tag != null) {
                                        x = var16 + 17 + index * 18;
                                        y = var17 + 7 - 33;

                                        UtilsFX.bindTexture(ModInfo.MODID, "textures/aspects/_back.png");
                                        GL11.glPushMatrix();
                                        GL11.glEnable(3042);
                                        GL11.glBlendFunc(770, 771);
                                        GL11.glTranslated(x + shiftx - 2, y + shifty - 2, 0.0D);
                                        GL11.glScaled(1.25D, 1.25D, 0.0D);

                                        UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                        GL11.glDisable(3042);
                                        GL11.glPopMatrix();
                                        if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), tag))
                                            UtilsFX.drawTag(x + shiftx, y + shifty, tag, tags.getAmount(tag), 0, UtilsFX.getGuiZLevel(gui));
                                        else {
                                            UtilsFX.bindTexture("textures/aspects/_unknown.png");
                                            GL11.glPushMatrix();
                                            GL11.glEnable(3042);
                                            GL11.glBlendFunc(770, 771);
                                            GL11.glTranslated(x + shiftx, y + shifty, 0.0D);
                                            UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                            GL11.glDisable(3042);
                                            GL11.glPopMatrix();
                                        }
                                        index++;
                                    }
                                }

                                for (Aspect tag : tags.getAspectsSortedAmount()) {
                                    if (tag != null) {
                                        x = var16 + 17 + index * 18;
                                        y = var17 + 7 - 33;

                                        UtilsFX.bindTexture("textures/aspects/_back.png");
                                        GL11.glPushMatrix();
                                        GL11.glEnable(3042);
                                        GL11.glBlendFunc(770, 771);
                                        GL11.glTranslated(x + shiftx - 2, y + shifty - 2, 0.0D);
                                        GL11.glScaled(1.25D, 1.25D, 0.0D);
                                        UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                        GL11.glDisable(3042);
                                        GL11.glPopMatrix();
                                        if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), tag))
                                            UtilsFX.drawTag(x + shiftx, y + shifty, tag, tags.getAmount(tag), 0, UtilsFX.getGuiZLevel(gui));
                                        else {
                                            UtilsFX.bindTexture("textures/aspects/_unknown.png");
                                            GL11.glPushMatrix();
                                            GL11.glEnable(3042);
                                            GL11.glBlendFunc(770, 771);
                                            GL11.glTranslated(x + shiftx, y + shifty, 0.0D);
                                            UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                            GL11.glDisable(3042);
                                            GL11.glPopMatrix();
                                        }
                                        index++;
                                    }
                                }
                            }
                            GL11.glEnable(2929);
                        }
                    }
                }
            }
        }
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private boolean isMouseOverSlot(Slot par1Slot, int par2, int par3, int par4, int par5) {
        int var4 = par4;
        int var5 = par5;
        par2 -= var4;
        par3 -= var5;
        return (par2 >= par1Slot.xDisplayPosition - 1) && (par2 < par1Slot.xDisplayPosition + 16 + 1) && (par3 >= par1Slot.yDisplayPosition - 1) && (par3 < par1Slot.yDisplayPosition + 16 + 1);
    }
}
