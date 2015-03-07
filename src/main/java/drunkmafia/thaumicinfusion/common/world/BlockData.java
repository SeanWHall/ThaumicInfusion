package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlockData extends BlockSavable implements IBlockHook {

    public NBTTagCompound tileTag;
    public World world;
    private TileEntity tile;
    private Map<String, Integer> methodsToBlock = new HashMap<String, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();

    public BlockData() {}

    public BlockData(WorldCoord coords, Class[] list) {
        super(coords);

        for (AspectEffect effect : classesToEffects(list)) {
            if(tile == null && effect.getClass().getAnnotation(Effect.class).hasTileEntity())
                tile = effect.getTile();
            effect.data = this;
            dataEffects.add(effect);
        }
    }

    @Override
    public void dataLoad(World world) {
        if(world == null)
            return;

        this.world = world;
        WorldCoord pos = getCoords();

        if (tile != null)
            world.setTileEntity(pos.x, pos.y, pos.z, tile);

        for(int a = 0; a < dataEffects.size(); a++) {
            AspectEffect effect = dataEffects.get(a);
            effect.aspectInit(world, getCoords());
            effect.data = this;

            for(String method : effect.getMethods())
                methodsToBlock.put(method, dataEffects.indexOf(effect));
        }
        init = true;
    }

    @Override
    public void setCoords(WorldCoord newPos) {
        super.setCoords(newPos);
        for(AspectEffect effect : dataEffects)
            effect.setCoords(newPos);
        if(world != null)
            TIWorldData.getWorldData(world).markDirty();
    }

    public void tickData() {
        if (tileTag != null && world.getBlock(coordinates.x, coordinates.y, coordinates.z) != null) {
            world.setTileEntity(coordinates.x, coordinates.y, coordinates.z, TileEntity.createAndLoadEntity(tileTag));
            tileTag = null;
        }
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

        if(tile != null){
            NBTTagCompound tileTag = new NBTTagCompound();
            tile.writeToNBT(tileTag);
            tagCompound.setTag("Tile", tileTag);
        }
    }

    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        for (int i = 0; i < tagCompound.getInteger("length"); i++)
            dataEffects.add((AspectEffect)SavableHelper.loadDataFromNBT(tagCompound.getCompoundTag("effect: " + i)));

        if(tagCompound.hasKey("Tile"))
            tile = TileEntity.createAndLoadEntity(tagCompound.getCompoundTag("Tile"));
    }

    @Override
    public String[] hookMethods() {
        Set key = methodsToBlock.keySet();
        return (String[]) key.toArray(new String[key.size()]);
    }

    @Override
    public Block getBlock(String method) {
        Integer index = methodsToBlock.get(method);
        if (index != null)
            return dataEffects.get(index);
        return null;
    }
}
