/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect;

import drunkmafia.thaumicinfusion.client.gui.aspect.EffectGui;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.asm.BlockTransformer;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AspectHandler {

    private static final Map<Aspect, Class<? extends AspectEffect>> registeredEffects = new HashMap<Aspect, Class<? extends AspectEffect>>();
    private static final Map<Aspect, Aspect[]> opposites = new HashMap<Aspect, Aspect[]>();

    private static ArrayList<Class<? extends AspectEffect>> effectsToRegister = new ArrayList<Class<? extends AspectEffect>>();
    private static Map<Class<? extends AspectEffect>, Integer> effectToCost = new HashMap<Class<? extends AspectEffect>, Integer>();

    /**
     * Registers an Effect, this has to be done during the pre-initialization
     *
     * @param effect The effect you want to register
     */
    public static void registerEffect(Class<? extends AspectEffect> effect) {
        Logger logger = ThaumicInfusion.getLogger();
        if (AspectHandler.isInCorretState(LoaderState.PREINITIALIZATION)) {
            logger.warn("Aspect registering cannot be called outside the pre init event");
            return;
        }

        if (effect != null && effect.isAnnotationPresent(Effect.class)) {
            try {
                Effect annotation = effect.getAnnotation(Effect.class);
                AspectEffect effectInstace = effect.newInstance();

                if (AspectHandler.effectsToRegister.contains(effect)) {
                    logger.error("Failed to register Effect: " + annotation.aspect());
                    return;
                }

                boolean isDef = annotation.aspect().equals("default");

                Configuration config = ThaumicInfusion.instance.config;
                config.load();
                effectInstace.readConfig(config);
                if (effectInstace.shouldRegister() && !isDef) AspectHandler.effectsToRegister.add(effect);
                config.save();

            } catch (Throwable e) {
                ThaumicInfusion.getLogger().error("Aspect: " + effect.getSimpleName() + " has caused an exception!", e);
            }
        }
    }

    protected static void setCost(Class<? extends AspectEffect> effect, int cost) {
        effectToCost.put(effect, cost);
    }

    public static int getCost(Class<? extends AspectEffect> effect) {
        return effectToCost.get(effect);
    }

    /**
     * Calculates the opposites of every effect and other misc stuff required to make the mod work
     */
    public static void postInit() {
        Logger logger = ThaumicInfusion.getLogger();
        if (AspectHandler.isInCorretState(LoaderState.POSTINITIALIZATION)) {
            logger.warn("Post Init cannot be called outside it's state");
            return;
        }

        for (Class<? extends AspectEffect> effect : AspectHandler.effectsToRegister) {
            Effect annotation = effect.getAnnotation(Effect.class);
            Aspect aspect = Aspect.getAspect(annotation.aspect().toLowerCase());
            if (aspect != null) {
                if (!AspectHandler.registeredEffects.containsKey(aspect) && isEffectSafe(effect)) {
                    AspectHandler.registeredEffects.put(aspect, effect);
                    logger.info("Registered Aspect Effect: " + aspect.getName());
                }
            } else logger.log(Level.ERROR, "Aspect: " + annotation.aspect() + " does not exist in the instance");
        }

        for (Aspect aspect : AspectHandler.getRegisteredAspects()) {
            AspectHandler.opposites.put(aspect, AspectHandler.calculateEffectOpposites(aspect));
        }

        logger.info(AspectHandler.registeredEffects.size() + " effects have been binded to their aspect, Failed to find: " + (AspectHandler.effectsToRegister.size() - AspectHandler.registeredEffects.size()) + " effects aspects.");
        AspectHandler.effectsToRegister = null;
    }

    /**
     * Internal use, calculates incompatible effects to ensure that effects that dont
     * work with each are able to be infused with into a block together
     *
     * @param aspect The Aspect you want to check
     * @return The opposites of the aspect
     */
    private static Aspect[] calculateEffectOpposites(Aspect aspect) {
        try {
            ArrayList<Aspect> aspects = new ArrayList<Aspect>();
            AspectEffect effect = AspectHandler.getEffectFromAspect(aspect).newInstance();
            for (Aspect checking : AspectHandler.getRegisteredAspects()) {
                for (Method method : AspectHandler.getEffectFromAspect(checking).getDeclaredMethods()) {
                    if (effect.hasMethod(method.getName())) {
                        aspects.add(checking);
                        break;
                    }
                }
            }
            return aspects.toArray(new Aspect[aspects.size()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Aspect[0];
    }

    private static boolean isEffectSafe(Class<? extends AspectEffect> effect) {
        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(Launch.classLoader.getClassBytes(effect.getName().replace('/', '.'))).accept(classNode, ClassReader.EXPAND_FRAMES);

            if (classNode.superName == null) return false;

            for (MethodNode methodNode : classNode.methods) {
                if (!BlockTransformer.blockMethods.contains(methodNode.name))
                    continue;

                if (!isMethodSafe(classNode, methodNode)) {
                    ThaumicInfusion.getLogger().error("ERROR! Effect: " + effect.getSimpleName() + " | Method: " + methodNode.name + " | Super Call to Block detected, This would cause an infinite loop. Effect will not be registered!");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private static boolean isMethodSafe(ClassNode classNode, MethodNode methodNode) throws Exception {
        for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
            if (insnNode instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insnNode;

                if (methodInsn.owner.equals("drunkmafia/thaumicinfusion/common/aspect/AspectEffect") && methodInsn.name.equals(methodNode.name))
                    return false;

                if (methodInsn.owner.equals(classNode.superName) && methodInsn.name.equals(methodNode.name)) {
                    ClassNode methodOwner = new ClassNode();
                    new ClassReader(Launch.classLoader.getClassBytes(methodInsn.owner)).accept(methodOwner, ClassReader.EXPAND_FRAMES);

                    for (MethodNode ownerMethods : methodOwner.methods) {
                        if (ownerMethods.name.equals(methodNode.name))
                            return isMethodSafe(methodOwner, ownerMethods);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Internal function to ensure that the methods are called in order
     *
     * @param state The state you want to check
     * @return Whether it is in this current state
     */
    private static boolean isInCorretState(LoaderState state) {
        Loader loader = Loader.instance();
        return !loader.isInState(state) && loader.activeModContainer().getModId().matches(ModInfo.MODID);
    }

    /**
     * Check if a number of aspect can be infused together
     *
     * @param aspects Aspect you want to check
     * @return Are the aspect compatible with each other
     */
    public static boolean canInfuse(Aspect[] aspects) {
        for (Aspect aspect : aspects) {
            Aspect[] opposite = AspectHandler.opposites.get(aspect);
            if (opposite.length > 0) {
                for (Aspect checking : aspects) {
                    if (checking != aspect) {
                        for (Aspect checkingOpposite : opposite) {
                            if (checkingOpposite == checking)
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Gets the cost of an aspect, used when infusing
     *
     * @param aspect The aspect you want the cost of
     * @return The cost of the aspect
     */
    public static int getCostOfEffect(Aspect aspect) {
        Class<? extends AspectEffect> c = AspectHandler.getEffectFromAspect(aspect);
        try {
            return c == null || c.getAnnotation(Effect.class) == null ? -1 : c.newInstance().getCost();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Gets all the registered aspects
     *
     * @return An Array of all the aspects that have been registered with an effect
     */
    public static Aspect[] getRegisteredAspects() {
        Aspect[] aspects = AspectHandler.getAllAspects();
        List<Aspect> registeredAspect = new ArrayList<Aspect>();
        for (Aspect aspect : aspects) {
            if (AspectHandler.registeredEffects.containsKey(aspect))
                registeredAspect.add(aspect);
        }
        return registeredAspect.toArray(new Aspect[registeredAspect.size()]);
    }

    /**
     * Gets all aspects in Thaumcraft
     *
     * @return A Full array of all the aspects
     */
    public static Aspect[] getAllAspects() {
        return Aspect.aspects.values().toArray(new Aspect[1]);
    }

    /**
     * Converts an AspectEffect Class to its registered Aspect
     *
     * @param effect The class you want the aspect of
     * @return The Aspect registered with the effect
     */
    public static Aspect getAspectsFromEffect(Class effect) {
        if (effect.isAnnotationPresent(Effect.class)) {
            Effect annotation = (Effect) effect.getAnnotation(Effect.class);
            return Aspect.getAspect(annotation.aspect());
        }
        return null;
    }

    /**
     * Converts an Aspect to its registered aspect Effect
     *
     * @param aspects The aspect you want the effect of
     * @return The Aspect effect registered with the aspect
     */
    public static Class<? extends AspectEffect> getEffectFromAspect(Aspect aspects) {
        return AspectHandler.registeredEffects.get(aspects);
    }

    public static class EffectBundle {
        public int guiID;
        public Class<? extends EffectGui> gui;
        public Class<? extends AspectEffect> effect;
    }
}

