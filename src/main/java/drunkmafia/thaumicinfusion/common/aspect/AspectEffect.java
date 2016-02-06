/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.asm.BlockTransformer;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.*;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.BlockMethod;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.ISavable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import thaumcraft.api.internal.WorldCoordinates;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Effect(aspect = "default", cost = 0)
public abstract class AspectEffect extends Block implements ISavable {

    private static final HashMap<Class, ArrayList<AspectEffect.MethodInfo>> phasedMethods = new HashMap<Class, ArrayList<AspectEffect.MethodInfo>>();

    protected WorldCoordinates pos;
    private boolean shouldRegister;

    public AspectEffect() {
        super(Material.air);
    }

    public static void init() {
        AspectHandler.registerEffect(Aer.class);
        AspectHandler.registerEffect(Aqua.class);
        AspectHandler.registerEffect(Auram.class);
        AspectHandler.registerEffect(Alienis.class);
        AspectHandler.registerEffect(Bestia.class);

        AspectHandler.registerEffect(Cognitio.class);

        AspectHandler.registerEffect(Fabrico.class);

        AspectHandler.registerEffect(Gelum.class);

        AspectHandler.registerEffect(Humanus.class);

        AspectHandler.registerEffect(Ignis.class);
        AspectHandler.registerEffect(Motus.class);

        AspectHandler.registerEffect(Desiderium.class);
        AspectHandler.registerEffect(Lux.class);

        AspectHandler.registerEffect(Machina.class);
        AspectHandler.registerEffect(Herba.class);

        AspectHandler.registerEffect(Potentia.class);
        AspectHandler.registerEffect(Permutatio.class);

        AspectHandler.registerEffect(Spiritus.class);

        AspectHandler.registerEffect(Terra.class);

        AspectHandler.registerEffect(Vinculum.class);
        AspectHandler.registerEffect(Vitreus.class);
        AspectHandler.registerEffect(Volatus.class);

        EntityRegistry.registerModEntity(InfusedBlockFalling.class, "InfusedBlockFalling", 0, ThaumicInfusion.instance, 80, 3, true);
    }

    public static List<AspectEffect.MethodInfo> getMethods(Class<? extends AspectEffect> clazz) {
        List<AspectEffect.MethodInfo> methods = AspectEffect.phasedMethods.get(clazz);
        return methods != null ? methods : AspectEffect.parseForMethods(clazz);
    }

    private static List<AspectEffect.MethodInfo> parseForMethods(Class<? extends AspectEffect> c) {
        Method[] effectMethods = c.getMethods();
        ArrayList<AspectEffect.MethodInfo> meths = new ArrayList<AspectEffect.MethodInfo>();

        for (Method meth : effectMethods) {
            if (!BlockTransformer.blockMethods.contains(meth.getName()) || meth.getDeclaringClass() == Block.class)
                continue;

            BlockMethod block = meth.getAnnotation(BlockMethod.class);
            if (block != null)
                meths.add(new AspectEffect.MethodInfo(meth.getName().hashCode(), block));
        }

        AspectEffect.phasedMethods.put(c, meths);
        return meths;
    }

    public int getCost() {
        return AspectHandler.getCost(getClass());
    }

    public void readConfig(Configuration config) {
        config.load();
        this.shouldRegister = config.getBoolean(this.getClass().getSimpleName(), "Effects", true, "");
        AspectHandler.setCost(getClass(), config.getInt(this.getClass().getSimpleName(), "Effects Cost", getClass().getAnnotation(Effect.class).cost(), 0, 100, ""));
        config.save();
    }

    public void onRemoveEffect() {
    }

    public void aspectInit(World world, WorldCoordinates pos) {
        this.pos = pos;
    }

    public WorldCoordinates getPos() {
        return this.pos;
    }

    public void setCoords(WorldCoordinates newPos) {
        this.pos = newPos;
    }

    public boolean shouldRegister() {
        return this.shouldRegister;
    }

    public boolean hasMethod(String methName) {
        for (MethodInfo method : AspectEffect.getMethods(getClass()))
            if (method.methodID == methName.hashCode()) return true;
        return false;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        if (this.pos != null)
            this.pos.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        this.pos = new WorldCoordinates();
        this.pos.readNBT(tagCompound);
    }

    public void onPlaceEffect(EntityPlayer player) {
    }

    public static class MethodInfo {

        public int methodID;
        public BlockMethod override;

        public MethodInfo(int methodID, BlockMethod override) {
            this.methodID = methodID;
            this.override = override;
        }
    }
}