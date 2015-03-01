package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlockData extends BlockSavable {

    private int containingID;
    private TileEntity tile;
    public World world;

    private Map<String, Integer> methodsToBlock = new HashMap<String, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();

    public BlockData() {}

    public BlockData(WorldCoord coords, Class[] list, int containingID, int blockID) {
        super(coords, blockID);
        this.containingID = containingID;

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
        if(!world.isRemote && BlockHandler.isBlockBlacklisted(getContainingBlock())){
            TIWorldData.getWorldData(world).removeBlock(getCoords(), true);
            return;
        }

        WorldCoord pos = getCoords();

        if(tile != null) {
            world.setTileEntity(pos.x, pos.y, pos.z, tile);
        }

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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockData && ((BlockSavable)obj).blockID == this.blockID && ((BlockData)obj).containingID == containingID;
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

    /** Can only be run within a method from the block class **/
    public Block runBlockMethod(){
        StackTraceElement lastMethod = Thread.currentThread().getStackTrace()[2];
        if(!BlockHandler.isBlockMethod(lastMethod.getMethodName()))
            throw new IllegalArgumentException("Attempted to run a block method outside of a block class, culprit class: " + lastMethod.getClassName() + " from: " + lastMethod.getMethodName());

        Integer index = methodsToBlock.get(lastMethod.getMethodName());
        return index != null ? dataEffects.get(index) : getContainingBlock();
    }

    public AspectEffect[] runAllAspectMethod(){
        String methName = Thread.currentThread().getStackTrace()[2].getMethodName();
        ArrayList<AspectEffect> effects = new ArrayList<AspectEffect>();
        for (AspectEffect dataEffect : dataEffects)
            if (dataEffect.hasMethod(methName))
                effects.add(dataEffect);
        return effects.toArray(new AspectEffect[effects.size()]);
    }

    public AspectEffect runAspectMethod(){
        String methName = Thread.currentThread().getStackTrace()[2].getMethodName();
        for (AspectEffect effect : dataEffects)
            if (effect.hasMethod(methName))
                return effect;
        return null;
    }

    Boolean openGUI = null;

    public boolean canOpenGUI(){
        if(openGUI != null)
            return openGUI;


        for(AspectEffect effect : getEffects()){
            Effect annot = effect.getClass().getAnnotation(Effect.class);
            if(openGUI = annot.hasGUI())
                return true;
        }
        return false;
    }

    public void setContainingBlock(Block block){containingID = Block.getIdFromBlock(block);}

    public Block getContainingBlock() {
        return Block.getBlockById(containingID);
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

        tagCompound.setInteger("ContainingID", containingID);

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
        containingID = tagCompound.getInteger("ContainingID");

        if(tagCompound.hasKey("Tile"))
            tile = TileEntity.createAndLoadEntity(tagCompound.getCompoundTag("Tile"));
    }
}
