package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.registry.EntityRegistry;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.*;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.ISavable;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.lang.annotation.Annotation;
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
        AspectHandler.registerEffect(Aer.class);
        AspectHandler.registerEffect(Alienis.class);
        AspectHandler.registerEffect(Aqua.class);
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
        if(pos != null){
            pos.id = getClass().getSimpleName();
            return pos;
        }
        return null;
    }

    public void setCoords(WorldCoord newPos){
        pos = newPos;
    }

    public boolean shouldRegister(){
        return true;
    }

    public void worldBlockInteracted(EntityPlayer player, World world, int x, int y, int z, int face) {}

    public void updateBlock(World world){}

    public boolean hasMethod(String methName){
        return getMethods(getClass()).contains(methName);
    }

    public static List<String> getMethods(Class<? extends AspectEffect> clazz){
        List<String> methods = phasedMethods.get(clazz);
        return methods != null ? methods : phaseForMethods(clazz);
    }

    private static List<String> phaseForMethods(Class<? extends AspectEffect> c){
        Method[] effectMethods = c.getMethods();
        ArrayList<String> meths = new ArrayList<String>();
        for(Method meth : effectMethods) {
            if(meth.getDeclaringClass() == Block.class)
                continue;
            OverrideBlock block = meth.getAnnotation(OverrideBlock.class);
            if(block != null)
                meths.add(meth.getName());
        }
        System.out.println(c.getSimpleName() + ": Has " + meths.size() + " Block overrides!");
        phasedMethods.put(c, meths);
        return meths;
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
