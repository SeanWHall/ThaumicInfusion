package drunkmafia.thaumicinfusion.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.container.InfusionContainer;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.client.WandAspectPacketS;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class InfusionGui extends GuiContainer {
    private Image background;
    private Image parchment;

    private EntityPlayer player;
    private ItemStack wand;

    private ScrollRect scrollRect;

    public InfusionGui(EntityPlayer player, ItemStack wand) {
        super(new InfusionContainer(player.inventory));

        this.player = player;
        this.wand = wand;

        xSize = 190;
        ySize = 232;
    }

    @Override
    public void initGui() {
        super.initGui();
        NBTTagCompound tagCompound = wand.getTagCompound();
        Aspect selected = null;
        AspectSlot slot = null;
        if (tagCompound != null && tagCompound.hasKey("InfusionAspect"))
            selected = Aspect.getAspect(tagCompound.getString("InfusionAspect"));

        Aspect[] aspects = AspectHandler.getAspects();
        AspectList knownAspects = Thaumcraft.proxy.getPlayerKnowledge().getAspectsDiscovered(player.getCommandSenderName());
        List<AspectSlot> aspectSlots = new ArrayList<AspectSlot>();

        for (Aspect aspect : aspects) {
            if(knownAspects.getAmount(aspect) <= 0) continue;
            AspectSlot aspectSlot = new AspectSlot(aspect, 16, 16);
            aspectSlots.add(aspectSlot);
            if (aspect == selected) slot = aspectSlot;
        }

        background = new Image(new ResourceLocation(ModInfo.MODID, "textures/gui/gui_infusion.png"), 0, 0, 0, 0, xSize, ySize);
        parchment = new Image(new ResourceLocation("thaumcraft", "textures/misc/parchment3.png"), 145, -20, 0, 0, 150, 150);

        scrollRect = new ScrollRect(54, 14, 76, 76, new Image(background.image, 70, 93, 191, 7, 24, 8), new Image(background.image, 92, 93, 215, 7, 24, 8), aspectSlots.toArray(new AspectSlot[aspectSlots.size()]));
        scrollRect.selected = slot;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float tpf, int mouseX, int mouseY) {
        background.drawImage();
        if(scrollRect.selected != null) parchment.drawImage();
        scrollRect.drawScrollBackground(mouseX, mouseY);
        if(scrollRect.selected != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) guiLeft, (float) guiTop, 0.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            fontRendererObj.drawSplitString(ThaumicInfusion.translate("ti.effect_info." + scrollRect.selected.aspect.getName().toUpperCase()), parchment.x + 10, parchment.y + 5, width - 10, 1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedTime) {
        super.mouseClicked(mouseX, mouseY, clickedTime);
        scrollRect.onMouseClicked(mouseX, mouseY);
    }

    class AspectSlot {

        private Aspect aspect;
        private int width, height;

        public AspectSlot(Aspect aspect, int width, int height) {
            this.aspect = aspect;
            this.width = width;
            this.height = height;
        }
    }

    class Image {

        public ResourceLocation image;
        public int x, y, u, v, width, height;

        private Object[] objects;

        public Image(ResourceLocation image, int x, int y, int u, int v, int width, int height, Object... objects) {
            this.image = image;

            this.x = x;
            this.y = y;
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;

            this.objects = objects;
        }

        public void drawImage() {
            GL11.glPushMatrix();
            mc.renderEngine.bindTexture(image);
            GL11.glEnable(3042);
            drawTexturedModalRect(guiLeft + x, guiTop + y, u, v, width, height);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }

        public boolean isInRect(int mouseX, int mouseY) {
            return mouseX >= guiLeft + x && mouseX <= guiLeft + x + width && mouseY >= guiTop + y && mouseY <= guiTop + y + height;
        }
    }

    public class ScrollRect {
        public int xPos, yPos, width, height;

        private double xAmount, yAmount, xMargin, yMargin;

        private Image left, right;

        private int yIndex, maxYIndex;

        private AspectSlot selected;
        private AspectSlot[][] slots;

        public ScrollRect(int xPos, int yPos, int width, int height, Image left, Image right, AspectSlot... aspects) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.width = width;
            this.height = height;

            this.left = left;
            this.right = right;

            xAmount = width / aspects[0].width;
            yAmount = height / aspects[0].height;
            xMargin = 4;
            yMargin = 4;

            slots = new AspectSlot[(int) xAmount][(int) Math.ceil(aspects.length / xAmount)];

            maxYIndex = (int) (slots[0].length - Math.ceil(yAmount));

            int i = 0;
            for (int x = 0; x < slots.length; x++) {
                for (int y = 0; y < slots[x].length; y++) {
                    if (i >= aspects.length)
                        break;
                    slots[x][y] = aspects[i++];
                }
            }
        }

        public void onMouseClicked(int mouseX, int mouseY) {
            AspectSlot mouseOver = getMouseOver(mouseX, mouseY);
            if (mouseOver != null) {
                selected = mouseOver;
                if (player.inventory.getCurrentItem() != null)
                    ChannelHandler.instance().sendToServer(new WandAspectPacketS(player, player.inventory.currentItem, selected.aspect));
            }

            if (left.isInRect(mouseX, mouseY)) {
                if (yIndex > 0) {
                    yIndex -= yAmount;
                    if (yIndex < 0) yIndex = 0;
                    mc.renderViewEntity.worldObj.playSound(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ, "thaumcraft:key", 0.3F, 1.0F, false);
                }
            } else if (right.isInRect(mouseX, mouseY)) {
                if (yIndex < maxYIndex) {
                    yIndex += yAmount;
                    mc.renderViewEntity.worldObj.playSound(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ, "thaumcraft:key", 0.3F, 1.0F, false);
                }
            }
        }

        public AspectSlot getMouseOver(int mouseX, int mouseY) {
            for (int x = 0; x < slots.length; x++) {
                for (int y = 0; y < yAmount; y++) {
                    if (y + yIndex >= slots[x].length) break;

                    AspectSlot slot = slots[x][y + yIndex];
                    if (slot != null && mouseX >= guiLeft + xPos + x + x * (slot.width + xMargin) && mouseX <= guiLeft + xPos + x + x * slot.width + (slot.width + xMargin) && mouseY >= guiTop + yPos + y + y * (slot.height + yMargin) && mouseY <= guiTop + yPos + y + y * slot.height + (slot.height + yMargin))
                        return slot;
                }
            }
            return null;
        }

        public void drawScrollBackground(int mouseX, int mouseY) {
            AspectSlot mouseOver = getMouseOver(mouseX, mouseY);

            if (yIndex > 0) left.drawImage();
            if (yIndex < maxYIndex) right.drawImage();

            for (int x = 0; x < slots.length; x++) {
                for (int y = 0; y < yAmount; y++) {
                    if (y + yIndex >= slots[x].length) break;
                    AspectSlot slot = slots[x][y + yIndex];
                    if (y + yIndex < slots[x].length && slot != null) {
                        UtilsFX.drawTag((int) (guiLeft + xPos + x * (slot.width + xMargin)), (int) (guiTop + yPos + y * (slot.height + yMargin)), slot.aspect, AspectHandler.getCostOfEffect(slot.aspect), 0, 0.0D, slot.aspect.getBlend(), 1.0F, mouseOver != slot && selected != slot);
                    }
                }
            }

            if (mouseOver != null) {
                ArrayList<String> tooltip = new ArrayList<String>();
                tooltip.add(mouseOver.aspect.getName());
                GL11.glPushMatrix();
                drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
                GL11.glPopMatrix();
            }
        }
    }
}
