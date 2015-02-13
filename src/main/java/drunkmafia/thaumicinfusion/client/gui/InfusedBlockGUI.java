package drunkmafia.thaumicinfusion.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DrunkMafia on 12/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class InfusedBlockGUI extends GuiScreen {

    protected BlockData data;
    private World world;
    private ResourceLocation gui = new ResourceLocation(ModInfo.MODID, "textures/gui/InfusedGUI.png");
    private Slider slider;

    private AspectEffect currentEffect;
    private EffectGUI effectGUI;

    public int guiLeft, guiTop, xSize, ySize;

    public InfusedBlockGUI(WorldCoord coordinates) {
        xSize = 180;
        ySize = 104;

        world = FMLClientHandler.instance().getClient().theWorld;
        data = TIWorldData.getData(BlockData.class, world, coordinates);
    }

    public void setupEffect(AspectEffect effect){
        currentEffect = effect;
        effectGUI = currentEffect.getGUI();
        effectGUI.xSize = xSize;
        effectGUI.ySize = ySize;
        effectGUI.initGui();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        Aspect[] effectGUIS;
        ArrayList<Aspect> temp = new ArrayList<Aspect>();
        for(AspectEffect effect : data.getEffects()){
            Effect anot = effect.getClass().getAnnotation(Effect.class);
            if(anot.hasGUI())
                temp.add(AspectHandler.getAspectsFromEffect(effect.getClass()));
        }
        effectGUIS = temp.toArray(new Aspect[temp.size()]);

        slider = new Slider(this, (guiLeft + (xSize / 2)) - (118 / 2), guiTop + ySize + 10, effectGUIS);
        setupEffect(effectFromAspect(slider.getSelectedAspect()));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float tpf) {
        super.drawScreen(mouseX, mouseY, tpf);
        drawGuiContainerBackgroundLayer(mouseX, mouseY, tpf);
        GL11.glDisable(2896);
        this.drawGuiContainerForegroundLayer(mouseX, mouseY, tpf);
        GL11.glEnable(2896);
    }

    protected void drawGuiContainerBackgroundLayer(int mouseX, int mouseY, float tpf) {
        if (data != null) {
            Minecraft.getMinecraft().renderEngine.bindTexture(gui);
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

            effectGUI.drawGuiContainerBackgroundLayer(mouseX, mouseY, tpf);
            slider.drawGuiContainerBackgroundLayer(tpf, mouseX, mouseY);
        }
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float tpf) {
        if (data != null) {
            Aspect aspect = AspectHandler.getAspectsFromEffect(currentEffect.getClass());
            fontRendererObj.drawString(aspect.getName(), guiLeft + 7, guiTop + 7, aspect.getColor());
            effectGUI.drawGuiContainerForegroundLayer(mouseX, mouseY, tpf);

            slider.drawGuiContainerForegroundLayer(mouseX, mouseY);
        }
    }

    void drawBackground(int x, int y, int width, int height){


    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int lastButtonClicked) {
        super.mouseClicked(mouseX, mouseY, lastButtonClicked);
        slider.mouseClicked(mouseX, mouseY, lastButtonClicked);
        setupEffect(effectFromAspect(slider.getSelectedAspect()));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick) {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        slider.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        setupEffect(effectFromAspect(slider.getSelectedAspect()));
    }


    public void drawTooltop(List text, int x, int y, FontRenderer font) {
        this.drawHoveringText(text, x, y, font);
    }

    protected FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    protected AspectEffect effectFromAspect(Aspect aspect){
        Class effectClass = AspectHandler.getEffectFromAspect(aspect);
        for(AspectEffect effect : data.getEffects())
            if(effect.getClass() == effectClass)
                return effect;

        return null;
    }
}
