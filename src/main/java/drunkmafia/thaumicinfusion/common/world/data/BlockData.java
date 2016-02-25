/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.asm.BlockTransformer;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect.MethodInfo;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.event.CommonEventContainer;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.helper.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.IServerTickable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.internal.WorldCoordinates;

import java.util.List;

public class BlockData extends BlockSavable implements IBlockHook {

    public TIWorldData worldData;

    private AspectEffect[] dataEffects = new AspectEffect[0];
    private MethodBlock[] indexToEffect = new MethodBlock[BlockTransformer.blockMethods.size()];
    private MethodBlock lastBlock;

    public BlockData() {
    }

    public BlockData(WorldCoordinates coords, Class[] list) {
        super(coords);

        dataEffects = this.classesToEffects(list);
    }

    private static int[] toPrimitive(Integer[] IntegerArray) {
        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i];
        }
        return result;
    }

    @Override
    public void dataUnload() {
        for (AspectEffect effect : dataEffects) {
            if (effect instanceof IServerTickable)
                CommonEventContainer.worldTickables.get(coordinates.dim).remove(effect);

            if (effect != null) effect.onRemoveEffect();
        }
    }

    @Override
    public void dataLoad(World world) {
        super.dataLoad(world);

        if (world == null)
            return;

        this.worldData = TIWorldData.getWorldData(world);

        for (int a = 0; a < this.dataEffects.length; a++) {
            AspectEffect effect = this.dataEffects[a];
            if (effect == null || AspectHandler.getAspectsFromEffect(effect.getClass()) == null) {
                ThaumicInfusion.getLogger().error("NULL EFFECT! An effect has been removed or failed to load, the data at: " + this.getCoords() + " has been removed!");
                TIWorldData.getWorldData(world).removeData(BlockData.class, this.getCoords(), true);
                return;
            }

            effect.aspectInit(world, this.getCoords());

            if (effect instanceof IServerTickable)
                CommonEventContainer.worldTickables.get(world.provider.getDimensionId()).add((IServerTickable) effect);

            List<MethodInfo> effectMethods = AspectEffect.getMethods(effect.getClass());
            for (MethodInfo method : effectMethods) {
                for (int i = 0; i < BlockTransformer.blockMethods.size(); i++) {
                    String methodName = BlockTransformer.blockMethods.get(i);
                    if (methodName.hashCode() == method.methodID)
                        indexToEffect[i] = new MethodBlock(a, method.override);
                }
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

    public void addEffect(Class<? extends AspectEffect>[] classes) {
        dataEffects = this.classesToEffects(classes);

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
        return dataEffects;
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
        tagCompound.setInteger("length", this.dataEffects.length);
        for (int i = 0; i < this.dataEffects.length; i++)
            tagCompound.setTag("effect: " + i, SavableHelper.saveDataToNBT(this.dataEffects[i]));
    }

    /* Removed until needed
    @Override
    public int[] hookMethods(Block block) {
        return this.methods;
    }
    */

    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        this.dataEffects = new AspectEffect[tagCompound.getInteger("length")];

        for (int i = 0; i < dataEffects.length; i++)
            this.dataEffects[i] = SavableHelper.loadDataFromNBT(tagCompound.getCompoundTag("effect: " + i));
    }

    @Override
    public Block getBlock(int method) {
        lastBlock = indexToEffect[method];
        if (lastBlock == null) return null;
        return dataEffects[lastBlock.blockIndex];
    }

    @Override
    public boolean shouldOverride(int method) {
        return lastBlock != null && lastBlock.override.overrideBlockFunc();
    }
}

class MethodBlock {

    public int blockIndex;
    public BlockMethod override;

    public MethodBlock(int blockIndex, BlockMethod override) {
        this.blockIndex = blockIndex;
        this.override = override;
    }
}