package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.client.gui.EffectGUI;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.*;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DrunkMafia on 05/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "default", cost = 0)
public class AspectEffect extends Block {

    private static HashMap<Class, ArrayList<String>> phasedMethods = new HashMap<Class, ArrayList<String>>();
    private List<String> methods;

    public BlockData data;
    protected WorldCoord pos;

    public boolean isEnabled = true;

    public AspectEffect() {
        super(Material.air);
        if(!phasedMethods.containsKey(getClass()))
            phaseForMethods();
        this.methods = phasedMethods.get(getClass());
    }

    public static void init(){
        AspectHandler.registerEffect(AspectEffect.class);
        AspectHandler.registerEffect(Aer.class);
        AspectHandler.registerEffect(Alienis.class);
        AspectHandler.registerEffect(Aqua.class);
        AspectHandler.registerEffect(Arbor.class);
        AspectHandler.registerEffect(Bestia.class);
        AspectHandler.registerEffect(Cognitio.class);
        AspectHandler.registerEffect(Corpus.class);
        AspectHandler.registerEffect(Fabrico.class);
        AspectHandler.registerEffect(Fames.class);
        AspectHandler.registerEffect(Gelum.class);
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
        AspectHandler.registerEffect(Sensus.class);
        AspectHandler.registerEffect(Spiritus.class);
        AspectHandler.registerEffect(Superbia.class);
        AspectHandler.registerEffect(Tempestas.class);
        AspectHandler.registerEffect(Tenebrae.class);
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

    public InfusedBlock getBlock(){
        return new InfusedBlock(Material.rock);
    }

    @SideOnly(Side.CLIENT)
    public EffectGUI getGUI(){return null;}

    public TileEntity getTile(){return null;}

    @SideOnly(Side.CLIENT)
    public boolean shouldRender(World world, int x, int y, int z, RenderBlocks renderBlocks){
        return true;
    }

    public void blockHighlight(World world, int x, int y, int z, EntityPlayer player, MovingObjectPosition pos, float partialTicks){}

    public void worldBlockInteracted(EntityPlayer player, World world, int x, int y, int z, int face) {}

    public void updateBlock(World world){}

    public static AspectEffect loadDataFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("class")) return null;
        try {
            Class<?> c = Class.forName(tag.getString("class"));
            if (AspectEffect.class.isAssignableFrom(c)) {
                AspectEffect data = (AspectEffect) c.newInstance();
                data.readNBT(tag);
                return data;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public boolean hasMethod(String methName){
        return methods.contains(methName);
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
        tagCompound.setString("class", this.getClass().getCanonicalName());
        if(pos != null)
            pos.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        pos = new WorldCoord();
        pos.readNBT(tagCompound);
    }
}
