package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.registry.EntityRegistry;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.*;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.ISavable;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DrunkMafia on 05/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "default", cost = 0)
public class AspectEffect extends Block implements ISavable {

    private static HashMap<Class, ArrayList<String>> phasedMethods = new HashMap<Class, ArrayList<String>>();

    public BlockData data;
    public boolean isEnabled = true;
    protected WorldCoord pos;

    public AspectEffect() {
        super(Material.air);

    }

    public static void init(){
        AspectHandler.registerEffect(AspectEffect.class);
        AspectHandler.registerEffect(Aer.class);
        AspectHandler.registerEffect(Alienis.class);
        AspectHandler.registerEffect(Arbor.class);
        AspectHandler.registerEffect(Bestia.class);
        AspectHandler.registerEffect(Cognitio.class);
        AspectHandler.registerEffect(Exanimis.class);
        AspectHandler.registerEffect(Fabrico.class);
        AspectHandler.registerEffect(Fames.class);
        AspectHandler.registerEffect(Humanus.class);
        AspectHandler.registerEffect(Ignis.class);
        AspectHandler.registerEffect(Infernus.class);
        AspectHandler.registerEffect(Iter.class);
        AspectHandler.registerEffect(Limus.class);
        AspectHandler.registerEffect(Lucrum.class);
        AspectHandler.registerEffect(Lux.class);
        AspectHandler.registerEffect(Machina.class);
        AspectHandler.registerEffect(Messis.class);
        AspectHandler.registerEffect(Mortuus.class);
        AspectHandler.registerEffect(Motus.class);
        AspectHandler.registerEffect(Ordo.class);
        AspectHandler.registerEffect(Pannus.class);
        AspectHandler.registerEffect(Perditio.class);
        AspectHandler.registerEffect(Permutatio.class);
        AspectHandler.registerEffect(Potentia.class);
        AspectHandler.registerEffect(Praecantatio.class);
        AspectHandler.registerEffect(Sano.class);
        AspectHandler.registerEffect(Spiritus.class);
        AspectHandler.registerEffect(Tempestas.class);
        AspectHandler.registerEffect(Terra.class);
        AspectHandler.registerEffect(Tutamen.class);
        AspectHandler.registerEffect(Vacuos.class);
        AspectHandler.registerEffect(Venenum.class);
        AspectHandler.registerEffect(Victus.class);
        AspectHandler.registerEffect(Vinculum.class);
        AspectHandler.registerEffect(Vitium.class);
        AspectHandler.registerEffect(Vitreus.class);
        AspectHandler.registerEffect(Volatus.class);

        EntityRegistry.registerModEntity(InfusedBlockFalling.class, "InfusedBlockFalling", 0, ThaumicInfusion.instance, 80, 3, true);
    }

    public void aspectInit(World world, WorldCoord pos){
        this.pos = pos;
    }

    public WorldCoord getPos(){
        return pos;
    }

    public void setCoords(WorldCoord newPos){
        pos = newPos;
    }

    public boolean shouldRegister(){
        return true;
    }

    public TileEntity getTile(){return null;}

    public void worldBlockInteracted(EntityPlayer player, World world, int x, int y, int z, int face) {}

    public void updateBlock(World world){}

    public boolean hasMethod(String methName){
        return getMethods().contains(methName);
    }

    public List<String> getMethods(){
        if(!phasedMethods.containsKey(getClass()))
            phaseForMethods();
        return phasedMethods.get(getClass());
    }

    public void phaseForMethods(){
        Class c = this.getClass();
        Method[] effectMethods = c.getDeclaredMethods();
        ArrayList<String> meths = new ArrayList<String>();
        for(Method meth : effectMethods)
            meths.add(meth.getName());
        phasedMethods.put(getClass(), meths);
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        if(pos != null)
            pos.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        pos = new WorldCoord();
        pos.readNBT(tagCompound);
    }
}
