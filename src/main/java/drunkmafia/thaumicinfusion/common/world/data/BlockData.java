/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect.MethodInfo;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockData extends BlockSavable implements IBlockHook {

    public TIWorldData worldData;
    private Map<Integer, BlockMethod> methodsOverrides = new HashMap<Integer, BlockMethod>();
    private Map<Integer, Integer> methodsToBlock = new HashMap<Integer, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();

    public BlockData() {
    }

    public BlockData(WorldCoordinates coords, Class[] list) {
        super(coords);

        for (AspectEffect effect : this.classesToEffects(list)) {
            if (effect != null) this.dataEffects.add(effect);
        }
    }

    private static int[] toPrimitive(Integer[] IntegerArray) {
        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i];
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
            if (effect == null || AspectHandler.getAspectsFromEffect(effect.getClass()) == null) {
                ThaumicInfusion.getLogger().error("NULL EFFECT! An effect has been removed or failed to load, the data at: " + this.getCoords() + " has been removed!");
                TIWorldData.getWorldData(world).removeData(BlockData.class, this.getCoords(), true);
                return;
            }

            effect.aspectInit(world, this.getCoords());

            List<MethodInfo> effectMethods = AspectEffect.getMethods(effect.getClass());
            for (MethodInfo method : effectMethods) {
                this.methodsOverrides.put(method.methodID, method.override);
                this.methodsToBlock.put(method.methodID, this.dataEffects.indexOf(effect));
            }
        }

        //this.methods = BlockData.toPrimitive(this.methodsToBlock.keySet().toArray(new Integer[this.methodsToBlock.keySet().size()]));
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
            if (effect != null) this.dataEffects.add(effect);
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

    /* Removed until needed
    @Override
    public int[] hookMethods(Block block) {
        return this.methods;
    }
    */

    @Override
    public Block getBlock(int method) {
        Integer index = this.methodsToBlock.get(method);
        return index != null ? this.dataEffects.get(index) : null;
    }

    @Override
    public boolean shouldOverride(int method) {
        return this.methodsOverrides.get(method) != null && this.methodsOverrides.get(method).overrideBlockFunc();
    }
}