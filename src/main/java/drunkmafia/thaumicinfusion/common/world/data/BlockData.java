/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.ISavable;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;

import java.util.*;

public class BlockData extends BlockSavable implements IBlockHook {

    public World world;
    private String[] methods = new String[0];
    private Map<String, OverrideBlock> methodsOverrides = new HashMap<String, OverrideBlock>();
    private Map<String, Integer> methodsToBlock = new HashMap<String, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();

    public BlockData() {}

    public BlockData(WorldCoordinates coords, Class[] list) {
        super(coords);

        for (AspectEffect effect : classesToEffects(list)) {
            if (effect == null) continue;
            effect.data = this;
            dataEffects.add(effect);
        }
    }

    @Override
    public void dataLoad(World world) {
        super.dataLoad(world);

        if(world == null)
            return;

        this.world = world;
        for(int a = 0; a < dataEffects.size(); a++) {
            AspectEffect effect = dataEffects.get(a);
            if (effect == null) {
                ThaumicInfusion.getLogger().error("NULL EFFECT! An effect has been removed or failed to load, the data at: " + getCoords() + " has been removed!");
                TIWorldData.getWorldData(world).removeData(BlockData.class, getCoords(), true);
                return;
            }

            effect.aspectInit(world, getCoords());
            effect.data = this;

            List<AspectEffect.MethodInfo> effectMethods = AspectEffect.getMethods(effect.getClass());
            for (AspectEffect.MethodInfo method : effectMethods) {
                methodsOverrides.put(method.methodName, method.override);
                methodsToBlock.put(method.methodName, dataEffects.indexOf(effect));
            }
        }
        Set key = methodsToBlock.keySet();
        methods = (String[]) key.toArray(new String[key.size()]);
    }

    @Override
    public void setCoords(WorldCoordinates newPos) {
        super.setCoords(newPos);
        for(AspectEffect effect : dataEffects)
            effect.setCoords(newPos);
    }

    public <T extends AspectEffect>T getEffect(Class<T> effect){
        for(AspectEffect obj : dataEffects)
            if(obj.getClass() == effect)
                return effect.cast(obj);
        return null;
    }

    public boolean hasEffect(Class<? extends AspectEffect> effect){
        return getEffect(effect) != null;
    }

    private AspectEffect[] classesToEffects(Class[] list) {
        AspectEffect[] effects = new AspectEffect[list.length];
        for (int i = 0; i < effects.length; i++) {
            try {
                AspectEffect eff = (AspectEffect) list[i].newInstance();
                eff.data = this;
                effects[i] = eff;
            }catch (Exception e){}
        }
        return effects;
    }

    public AspectEffect[] getEffects() {
        AspectEffect[] classes = new AspectEffect[dataEffects.size()];
        return dataEffects.toArray(classes);
    }

    public Aspect[] getAspects(){
        AspectEffect[] effects = getEffects();
        Aspect[] aspects = new Aspect[effects.length];
        for(int i = 0; i < effects.length; i++)
            aspects[i] = AspectHandler.getAspectsFromEffect(effects[i].getClass());

        return aspects;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setInteger("length", dataEffects.size());
        for (int i = 0; i < dataEffects.size(); i++)
            tagCompound.setTag("effect: " + i, SavableHelper.saveDataToNBT(dataEffects.get(i)));
    }

    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        for (int i = 0; i < tagCompound.getInteger("length"); i++)
            dataEffects.add((AspectEffect)SavableHelper.loadDataFromNBT(tagCompound.getCompoundTag("effect: " + i)));
    }

    @Override
    public String[] hookMethods(Block block) {
        return methods;
    }

    @Override
    public Block getBlock(String method) {
        Integer index = methodsToBlock.get(method);
        if (index != null)
            return dataEffects.get(index);
        return null;
    }

    @Override
    public boolean shouldOverride(String method) {
        return methodsOverrides.get(method) != null && methodsOverrides.get(method).overrideBlockFunc();
    }
}
