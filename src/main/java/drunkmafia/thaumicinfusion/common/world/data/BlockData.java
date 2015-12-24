/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect.MethodInfo;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.IClientTickable;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.internal.WorldCoordinates;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.wands.ItemWand;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockData extends BlockSavable implements IBlockHook {

    private static ResourceLocation[] textures;
    public TIWorldData worldData;
    public int ticksExisted;
    private AnimatedFrames infusionFrames;
    private int[] methods = new int[0];
    private Map<Integer, BlockMethod> methodsOverrides = new HashMap<Integer, BlockMethod>();
    private Map<Integer, Integer> methodsToBlock = new HashMap<Integer, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();
    private int tick, colour = 0;

    public BlockData() {
    }

    public BlockData(WorldCoordinates coords, Class[] list) {
        super(coords);

        for (AspectEffect effect : this.classesToEffects(list)) {
            if (effect == null) continue;
            effect.data = this;
            this.dataEffects.add(effect);
        }
    }

    private static int[] toPrimitive(Integer[] IntegerArray) {
        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i].intValue();
        }
        return result;
    }

    @Override
    public void dataLoad(World world) {
        super.dataLoad(world);

        if (world == null)
            return;

        this.worldData = TIWorldData.getWorldData(world);

        this.methodsOverrides = new HashMap<Integer, BlockMethod>();
        this.methodsToBlock = new HashMap<Integer, Integer>();

        for (int a = 0; a < this.dataEffects.size(); a++) {
            AspectEffect effect = this.dataEffects.get(a);
            if (effect == null) {
                ThaumicInfusion.getLogger().error("NULL EFFECT! An effect has been removed or failed to load, the data at: " + this.getCoords() + " has been removed!");
                TIWorldData.getWorldData(world).removeData(BlockData.class, this.getCoords(), true);
                return;
            }

            effect.aspectInit(world, this.getCoords());
            effect.data = this;

            List<MethodInfo> effectMethods = AspectEffect.getMethods(effect.getClass());
            for (MethodInfo method : effectMethods) {
                this.methodsOverrides.put(method.methodID, method.override);
                this.methodsToBlock.put(method.methodID, this.dataEffects.indexOf(effect));
            }
        }
        this.methods = BlockData.toPrimitive(this.methodsToBlock.keySet().toArray(new Integer[this.methodsToBlock.keySet().size()]));
    }

    public void renderData(EntityPlayer player, float partialTicks) {
        int x = this.coordinates.pos.getX(), y = this.coordinates.pos.getY(), z = this.coordinates.pos.getZ();
        double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
        double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        TIWorldData worldData = TIWorldData.getWorldData(player.worldObj);
        if ((player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemWand.class) && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) != null && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing)) {
            if (infusionFrames == null) {
                if (textures == null) {
                    textures = new ResourceLocation[6];
                    for (int i = 0; i < textures.length; i++)
                        textures[i] = new ResourceLocation(ModInfo.MODID, "textures/blocks/infusion/" + (i + 1) + ".png");
                }
                infusionFrames = new AnimatedFrames(textures, 60);
            }

            if (getAspects().length > 1) {
                if (tick >= 120) {
                    int lastColour = colour;
                    boolean getNewColour = false;
                    for (Aspect aspect : getAspects()) {
                        if (getNewColour) {
                            colour = aspect.getColor();
                            getNewColour = false;
                            break;
                        }
                        if (aspect.getColor() == lastColour) getNewColour = true;
                    }
                    if (getNewColour) colour = getAspects()[0].getColor();
                    tick = 0;
                } else {
                    tick++;
                }
            }

            if (colour == 0) colour = getAspects()[0].getColor();

            Color cc = new Color(colour);
            for (int side = 0; side < 6; ++side) {
                EnumFacing dir = EnumFacing.values()[side];
                if (worldData.getBlock(BlockData.class, getCoords().pos.add(-dir.getFrontOffsetX(), -dir.getFrontOffsetY(), -dir.getFrontOffsetZ())) != null)
                    continue;

                GL11.glPushMatrix();

                GL11.glTranslated(-iPX + x + 0.5D, -iPY + y + 0.5D, -iPZ + z + 0.5D);
                GL11.glRotatef(90.0F, (float) (-dir.getFrontOffsetY()), (float) dir.getFrontOffsetX(), (float) (-dir.getFrontOffsetZ()));

                GL11.glTranslated(0.0D, 0.0D, dir.getFrontOffsetZ() < 0 ? 0.5F : -0.5D);

                GL11.glScaled(1.002D, 1.002D, 1.002D);
                if (side == 2) GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
                GL11.glTranslated(-0.001D, -0.001D, -0.001D);

                UtilsFX.renderQuadCentered(infusionFrames.getTexture(), 1, 1, 0, 1.0F, (float) cc.getRed() / 255.0F, (float) cc.getGreen() / 255.0F, (float) cc.getBlue() / 255.0F, 200, 1, 255);

                GL11.glPopMatrix();
            }
        }

        for (AspectEffect effect : this.getEffects()) {
            if (effect instanceof IClientTickable)
                ((IClientTickable) effect).clientTick(getWorld(), (int) -iPX + x, (int) -iPY + y, (int) -iPZ + z, partialTicks);
        }
    }

    @Override
    public void setCoords(WorldCoordinates newPos) {
        super.setCoords(newPos);
        for (AspectEffect effect : this.dataEffects)
            effect.setCoords(newPos);
    }

    public <T extends AspectEffect> T getEffect(Class<T> effect) {
        for (AspectEffect obj : this.dataEffects)
            if (obj.getClass() == effect)
                return effect.cast(obj);
        return null;
    }

    public void removeEffect(Class<? extends AspectEffect> effect) {
        for (AspectEffect aspectEffect : this.dataEffects) {
            if (!(aspectEffect.getClass() == effect)) continue;
            for (MethodInfo method : AspectEffect.getMethods(aspectEffect.getClass())) {
                this.methodsToBlock.remove(method.methodID);
                this.methodsOverrides.remove(method.methodID);
            }
            this.dataEffects.remove(aspectEffect);

            if (!getWorld().isRemote)
                ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(this), getWorld().provider.getDimensionId());

            return;
        }
    }

    public void addEffect(Class<? extends AspectEffect>[] classes) {
        for (AspectEffect effect : this.classesToEffects(classes)) {
            if (effect == null) continue;
            effect.data = this;
            this.dataEffects.add(effect);
        }

        if (!getWorld().isRemote)
            ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(this), getWorld().provider.getDimensionId());

        this.dataLoad(getWorld());
    }

    public boolean hasEffect(Class<? extends AspectEffect> effect) {
        return this.getEffect(effect) != null;
    }

    private AspectEffect[] classesToEffects(Class[] list) {
        AspectEffect[] effects = new AspectEffect[list.length];
        for (int i = 0; i < effects.length; i++) {
            try {
                AspectEffect eff = (AspectEffect) list[i].newInstance();
                eff.data = this;
                effects[i] = eff;
            } catch (Exception e) {
            }
        }
        return effects;
    }

    public World getWorld() {
        return worldData.world;
    }

    public AspectEffect[] getEffects() {
        AspectEffect[] classes = new AspectEffect[this.dataEffects.size()];
        return this.dataEffects.toArray(classes);
    }

    public Aspect[] getAspects() {
        AspectEffect[] effects = this.getEffects();
        Aspect[] aspects = new Aspect[effects.length];
        for (int i = 0; i < effects.length; i++) {
            if (effects[i] == null) continue;
            aspects[i] = AspectHandler.getAspectsFromEffect(effects[i].getClass());
        }

        return aspects;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setInteger("length", this.dataEffects.size());
        for (int i = 0; i < this.dataEffects.size(); i++)
            tagCompound.setTag("effect: " + i, SavableHelper.saveDataToNBT(this.dataEffects.get(i)));
    }

    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        this.dataEffects = new ArrayList<AspectEffect>();

        for (int i = 0; i < tagCompound.getInteger("length"); i++)
            this.dataEffects.add((AspectEffect) SavableHelper.loadDataFromNBT(tagCompound.getCompoundTag("effect: " + i)));
    }

    @Override
    public int[] hookMethods(Block block) {
        return this.methods;
    }

    @Override
    public Block getBlock(int method) {
        Integer index = this.methodsToBlock.get(method);
        return index != null ? this.dataEffects.get(index) : null;
    }

    @Override
    public boolean shouldOverride(int method) {
        return this.methodsOverrides.get(method) != null && this.methodsOverrides.get(method).overrideBlockFunc();
    }

    static class AnimatedFrames {

        int currentFrame = 0, fps, frameCounter = 0;
        private ResourceLocation[] frames;

        public AnimatedFrames(ResourceLocation[] frames, int fps) {
            this.frames = frames;
            this.fps = fps;
        }

        public ResourceLocation getTexture() {
            ResourceLocation response = frames[currentFrame];
            frameCounter++;
            if (frameCounter >= fps) {
                currentFrame++;
                frameCounter = 0;
                if (currentFrame >= frames.length) currentFrame = 0;
            }
            return response;
        }

    }
}
