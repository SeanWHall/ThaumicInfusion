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
    private Scroll parchment;

    private EntityPlayer player;
    private ItemStack wand;

    private ScrollRect scrollRect;

    public InfusionGui(EntityPlayer player, ItemStack wand) {
        super(new InfusionContainer());

        this.player = player;
        this.wand = wand;

        xSize = 97;
        ySize = 105;
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
        parchment = new Scroll(new ResourceLocation("thaumcraft", "textures/misc/parchment3.png"), xSize, ((ySize - 150) / 2), 0, 0, 150, 150);

        scrollRect = new ScrollRect(10, 10, 76, 76, new Image(background.image, 24, 89, 97, 0, 25, 8), new Image(background.image, 46, 89, 120, 0, 25, 8), aspectSlots.toArray(new AspectSlot[aspectSlots.size()]));
        scrollRect.selected = slot;
        if(slot != null) parchment.setAspect(slot.aspect);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float tpf, int mouseX, int mouseY) {
        background.drawImage();
        if(scrollRect.selected != null) parchment.drawImage();
        scrollRect.drawScrollBackground(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if(scrollRect.selected != null) parchment.drawForeground();
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

    class Scroll extends Image {

        private String desc;

        public Scroll(ResourceLocation image, int x, int y, int u, int v, int width, int height, Object... objects) {
            super(image, x, y, u, v, width, height, objects);
        }

        public void setAspect(Aspect aspect){
            desc = ThaumicInfusion.translate("ti.effect_info." + aspect.getName().toUpperCase());
        }

        public void drawForeground(){
            if(desc != null)
                fontRendererObj.drawSplitString(desc, x + 10, y + 5, width - 10, 1);
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
            drawTexturedModalRect(guiLeft + x, guiTop + y, u, v, width, height);
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
                parchment.setAspect(selected.aspect);
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
                drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
            }
        }
    }
}
