package drunkmafia.thaumicinfusion.client.gui;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.client.WandAspectPacketS;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.research.ResearchManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class InfusionGui extends TIGui {

    private final EntityPlayer player;
    private final ItemStack wand;
    private Image background;
    private Image parchment;
    private InfusionGui.ScrollRect normalScrollRect;

    public InfusionGui(EntityPlayer player, ItemStack wand) {
        this.player = player;
        this.wand = wand;

        this.xSize = 105;
        this.ySize = 113;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.background = new Image(this, new ResourceLocation(ModInfo.MODID, "textures/gui/gui_infusion.png"), 0, 0, 0, 0, this.xSize, this.ySize);
        this.parchment = new Image(this, new ResourceLocation(ModInfo.MODID, "textures/gui/parchment3.png"), 110, -20, 0, 0, 150, 150);

        NBTTagCompound tagCompound = this.wand.getTagCompound();
        Aspect selected = null;
        if (tagCompound != null && tagCompound.hasKey("InfusionAspect"))
            selected = Aspect.getAspect(tagCompound.getString("InfusionAspect"));

        this.normalScrollRect = this.getScrollRect(AspectHandler.getRegisteredAspects(), selected);
    }

    private InfusionGui.ScrollRect getScrollRect(Aspect[] aspects, Aspect selected) {
        AspectList knownAspects = new AspectList();

        for (Aspect aspect : AspectHandler.getAllAspects()) {
            for (String research : ResearchManager.getResearchForPlayer(player.getName())) {
                if (Aspect.getAspect(research.replace("!", "")) != null || aspect.isPrimal()) {
                    knownAspects.add(aspect, 1);
                    break;
                }
            }
        }

        List<InfusionGui.AspectSlot> aspectSlots = new ArrayList<InfusionGui.AspectSlot>();

        InfusionGui.AspectSlot slot = null;

        for (Aspect aspect : aspects) {
            if (knownAspects.getAmount(aspect) <= 0) continue;
            InfusionGui.AspectSlot aspectSlot = new InfusionGui.AspectSlot(aspect, 16, 16);
            aspectSlots.add(aspectSlot);
            if (aspect == selected) slot = aspectSlot;
        }

        InfusionGui.ScrollRect scrollRect = new InfusionGui.ScrollRect(14, 14, 76, 76, 16, 16, new Image(this, this.background.image, 28, 93, 191, 7, 24, 8), new Image(this, this.background.image, 52, 93, 215, 7, 24, 8), aspectSlots.toArray(new InfusionGui.AspectSlot[aspectSlots.size()]));
        scrollRect.selected = slot;
        return scrollRect;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tpf) {
        drawDefaultBackground();
        this.background.drawImage();

        if (this.normalScrollRect.selected != null) this.parchment.drawImage();
        this.normalScrollRect.drawScrollBackground(mouseX, mouseY);
        if (this.normalScrollRect.selected != null) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float) this.guiLeft, (float) this.guiTop, 0.0F);
            this.fontRendererObj.drawSplitString(ThaumicInfusion.translate("ti.effect_info." + this.normalScrollRect.selected.aspect.getName().toUpperCase()), this.parchment.x + 10, this.parchment.y + 5, this.parchment.width - 10, 1);
            GL11.glPopMatrix();
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedTime) throws IOException {
        super.mouseClicked(mouseX, mouseY, clickedTime);

        this.normalScrollRect.onMouseClicked(mouseX, mouseY);
        InfusionGui.AspectSlot slot = this.normalScrollRect.selected != null ? this.normalScrollRect.findAspect(this.normalScrollRect.selected.aspect) : null;
        if (this.normalScrollRect.selected != null)
            this.normalScrollRect.selected = slot;
        else if (this.normalScrollRect.selected == slot)
            this.normalScrollRect.selected = null;
    }

    class AspectSlot {

        private final Aspect aspect;
        private final int width;
        private final int height;

        public AspectSlot(Aspect aspect, int width, int height) {
            this.aspect = aspect;
            this.width = width;
            this.height = height;
        }
    }

    public class ScrollRect {
        private final double xAmount;
        private final double yAmount;
        private final double xMargin;
        private final double yMargin;
        private final Image left;
        private final Image right;
        private final int maxYIndex;
        private final InfusionGui.AspectSlot[][] slots;
        public int xPos, yPos, width, height;
        private int yIndex;
        private InfusionGui.AspectSlot selected;

        public ScrollRect(int xPos, int yPos, int width, int height, int aspectWidth, int aspectHeight, Image left, Image right, InfusionGui.AspectSlot... aspects) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.width = width;
            this.height = height;

            this.left = left;
            this.right = right;

            this.xAmount = width / aspectWidth;
            this.yAmount = height / aspectHeight;
            this.xMargin = 4;
            this.yMargin = 4;

            int totalY = (int) Math.ceil(aspects.length / this.xAmount);

            this.slots = new InfusionGui.AspectSlot[(int) this.xAmount][totalY];

            this.maxYIndex = (int) (this.slots[0].length - Math.ceil(this.yAmount));

            int i = 0;
            for (int x = 0; x < this.xAmount; x++) {
                this.slots[x] = new InfusionGui.AspectSlot[totalY];
                for (int y = 0; y < totalY; y++) {
                    if (i >= aspects.length)
                        break;
                    this.slots[x][y] = aspects[i++];
                }
            }
        }

        public void onMouseClicked(int mouseX, int mouseY) {
            InfusionGui.AspectSlot mouseOver = this.getMouseOver(mouseX, mouseY);
            if (mouseOver != null) {
                this.selected = mouseOver == this.selected ? null : mouseOver;
                if (InfusionGui.this.player.inventory.getCurrentItem() != null)
                    ChannelHandler.instance().sendToServer(new WandAspectPacketS(InfusionGui.this.player, InfusionGui.this.player.inventory.currentItem, this.selected != null ? this.selected.aspect : null, false));
            }

            if (this.left.isInRect(mouseX, mouseY)) {
                if (this.yIndex > 0) {
                    this.yIndex -= this.yAmount;
                    if (this.yIndex < 0) this.yIndex = 0;
                    InfusionGui.this.mc.getRenderViewEntity().worldObj.playSound(InfusionGui.this.mc.getRenderViewEntity().posX, InfusionGui.this.mc.getRenderViewEntity().posY, InfusionGui.this.mc.getRenderViewEntity().posZ, "thaumcraft:key", 0.3F, 1.0F, false);
                }
            } else if (this.right.isInRect(mouseX, mouseY)) {
                if (this.yIndex < this.maxYIndex) {
                    this.yIndex += this.yAmount;
                    InfusionGui.this.mc.getRenderViewEntity().worldObj.playSound(InfusionGui.this.mc.getRenderViewEntity().posX, InfusionGui.this.mc.getRenderViewEntity().posY, InfusionGui.this.mc.getRenderViewEntity().posZ, "thaumcraft:key", 0.3F, 1.0F, false);
                }
            }
        }

        public InfusionGui.AspectSlot getMouseOver(int mouseX, int mouseY) {
            for (int x = 0; x < this.slots.length; x++) {
                for (int y = 0; y < this.yAmount; y++) {
                    if (y + this.yIndex >= this.slots[x].length) break;

                    InfusionGui.AspectSlot slot = this.slots[x][y + this.yIndex];
                    if (slot != null && mouseX >= InfusionGui.this.guiLeft + this.xPos + x + x * (slot.width + this.xMargin) && mouseX <= InfusionGui.this.guiLeft + this.xPos + x + x * slot.width + (slot.width + this.xMargin) && mouseY >= InfusionGui.this.guiTop + this.yPos + y + y * (slot.height + this.yMargin) && mouseY <= InfusionGui.this.guiTop + this.yPos + y + y * slot.height + (slot.height + this.yMargin))
                        return slot;
                }
            }
            return null;
        }

        public void drawScrollBackground(int mouseX, int mouseY) {
            InfusionGui.AspectSlot mouseOver = this.getMouseOver(mouseX, mouseY);

            if (this.yIndex > 0) this.left.drawImage();
            if (this.yIndex < this.maxYIndex) this.right.drawImage();

            for (int x = 0; x < this.slots.length; x++) {
                for (int y = 0; y < this.yAmount; y++) {
                    if (y + this.yIndex >= this.slots[x].length) break;
                    InfusionGui.AspectSlot slot = this.slots[x][y + this.yIndex];
                    if (y + this.yIndex < this.slots[x].length && slot != null) {
                        UtilsFX.drawTag((int) (InfusionGui.this.guiLeft + this.xPos + x * (slot.width + this.xMargin)), (int) (InfusionGui.this.guiTop + this.yPos + y * (slot.height + this.yMargin)), slot.aspect, AspectHandler.getCostOfEffect(slot.aspect), 0, 0.0D, slot.aspect.getBlend(), 1.0F, mouseOver != slot && this.selected != slot);
                    }
                }
            }

            if (mouseOver != null) {
                ArrayList<String> tooltip = new ArrayList<String>();
                tooltip.add(mouseOver.aspect.getName());
                GL11.glPushMatrix();
                InfusionGui.this.drawHoveringText(tooltip, mouseX, mouseY, InfusionGui.this.fontRendererObj);
                GL11.glPopMatrix();
            }
        }

        private InfusionGui.AspectSlot findAspect(Aspect aspect) {
            for (InfusionGui.AspectSlot[] xSlots : this.slots) {
                for (InfusionGui.AspectSlot slot : xSlots) {
                    if (slot != null && aspect != null && slot.aspect.getTag().equals(aspect.getTag())) return slot;
                }
            }
            return null;
        }
    }
}
