/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.registry.EntityRegistry;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.*;
import drunkmafia.thaumicinfusion.common.aspect.entity.InfusedBlockFalling;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.ISavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import thaumcraft.api.WorldCoordinates;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Effect(aspect = "default", cost = 0)
public class AspectEffect extends Block implements ISavable {

    private static HashMap<Class, ArrayList<MethodInfo>> phasedMethods = new HashMap<Class, ArrayList<MethodInfo>>();
    public BlockData data;
    protected WorldCoordinates pos;
    private boolean shouldRegister;

    public AspectEffect() {
        super(Material.air);

    }

    public static void init(){
        AspectHandler.registerEffect(Aer.class);
        AspectHandler.registerEffect(Alienis.class);
        AspectHandler.registerEffect(Bestia.class);
        AspectHandler.registerEffect(Cognitio.class);
        AspectHandler.registerEffect(Exanimis.class);
        AspectHandler.registerEffect(Fabrico.class);
        AspectHandler.registerEffect(Fames.class);
        AspectHandler.registerEffect(Humanus.class);
        AspectHandler.registerEffect(Ignis.class);
        AspectHandler.registerEffect(Infernus.class);
        AspectHandler.registerEffect(Iter.class);
        AspectHandler.registerEffect(Lucrum.class);
        AspectHandler.registerEffect(Lux.class);
        AspectHandler.registerEffect(Machina.class);
        AspectHandler.registerEffect(Messis.class);
        AspectHandler.registerEffect(Mortuus.class);
        AspectHandler.registerEffect(Pannus.class);
        AspectHandler.registerEffect(Perditio.class);
        AspectHandler.registerEffect(Potentia.class);
        AspectHandler.registerEffect(Permutatio.class);
        AspectHandler.registerEffect(Praecantatio.class);
        AspectHandler.registerEffect(Sano.class);
        AspectHandler.registerEffect(Spiritus.class);
        AspectHandler.registerEffect(Tempestas.class);
        AspectHandler.registerEffect(Terra.class);
        AspectHandler.registerEffect(Tutamen.class);
        AspectHandler.registerEffect(Venenum.class);
        AspectHandler.registerEffect(Victus.class);
        AspectHandler.registerEffect(Vinculum.class);
        AspectHandler.registerEffect(Vitium.class);
        AspectHandler.registerEffect(Vitreus.class);
        AspectHandler.registerEffect(Volatus.class);

        EntityRegistry.registerModEntity(InfusedBlockFalling.class, "InfusedBlockFalling", 0, ThaumicInfusion.instance, 80, 3, true);
    }

    public static List<MethodInfo> getMethods(Class<? extends AspectEffect> clazz) {
        List<MethodInfo> methods = phasedMethods.get(clazz);
        return methods != null ? methods : phaseForMethods(clazz);
    }

    private static List<MethodInfo> phaseForMethods(Class<? extends AspectEffect> c) {
        Method[] effectMethods = c.getMethods();
        ArrayList<MethodInfo> meths = new ArrayList<MethodInfo>();
        for (Method meth : effectMethods) {
            if (meth.getDeclaringClass() == Block.class)
                continue;
            OverrideBlock block = meth.getAnnotation(OverrideBlock.class);
            if (block != null)
                meths.add(new MethodInfo(meth.getName(), block));
        }

        phasedMethods.put(c, meths);
        return meths;
    }

    public void syncBlockData() {
        ChannelHandler.network.sendToAll(new BlockSyncPacketC(data));
    }

    public void readConfig(Configuration config) {
        shouldRegister = config.getBoolean(getClass().getSimpleName(), "Effects", true, "");
    }

    public void aspectInit(World world, WorldCoordinates pos) {
        this.pos = pos;
    }

    public WorldCoordinates getPos() {
        return pos;
    }

    public void setCoords(WorldCoordinates newPos) {
        pos = newPos;
    }

    public boolean shouldRegister(){
        return true;
    }

    public void interactWithEntity(EntityPlayer player, Entity Target) {

    }

    public void worldBlockInteracted(EntityPlayer player, World world, int x, int y, int z, int face) {}

    public boolean hasMethod(String methName){
        return getMethods(getClass()).contains(methName);
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        if(pos != null)
            pos.writeNBT(tagCompound);
    }

    public void readNBT(NBTTagCompound tagCompound) {
        pos = new WorldCoordinates();
        pos.readNBT(tagCompound);
    }

    public static class MethodInfo {

        public String methodName;
        public OverrideBlock override;

        public MethodInfo(String methodName, OverrideBlock override) {
            this.methodName = methodName;
            this.override = override;
        }
    }
}