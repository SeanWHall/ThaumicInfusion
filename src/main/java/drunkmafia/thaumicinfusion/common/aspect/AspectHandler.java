/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import drunkmafia.thaumicinfusion.client.gui.aspect.EffectGui;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AspectHandler {

    private static ArrayList<Class<? extends AspectEffect>> effectsToRegister = new ArrayList<Class<? extends AspectEffect>>();
    private static Map<Aspect, Class<? extends AspectEffect>> registeredEffects = new HashMap<Aspect, Class<? extends AspectEffect>>();
    private static List<EffectBundle> guiEffects = new ArrayList<EffectBundle>();
    private static Map<Aspect, Aspect[]> opposites = new HashMap<Aspect, Aspect[]>();

    /**
     * Registers an Effect, this has to be done during the pre-initialization
     *
     * @param effect The effect you want to register
     */
    public static void registerEffect(Class<? extends AspectEffect> effect){
        Logger logger = ThaumicInfusion.getLogger();
        if(isInCorretState(LoaderState.PREINITIALIZATION)) {
            logger.warn("Aspect registering cannot be called outside the pre init event");
            return;
        }

        if (effect != null && effect.isAnnotationPresent(Effect.class)) {
            try {
                Effect annotation = effect.getAnnotation(Effect.class);
                AspectEffect effectInstace = effect.newInstance();

                if(effectsToRegister.contains(effect)){
                    logger.error("Failed to register Effect: " + annotation.aspect());
                    return;
                }

                boolean isDef = annotation.aspect().equals("default");

                Configuration config = ThaumicInfusion.instance.config;
                config.load();
                effectInstace.readConfig(config);
                if(effectInstace.shouldRegister() && !isDef) effectsToRegister.add(effect);
                config.save();

            }catch (Throwable e){
                ThaumicInfusion.getLogger().error("Aspect: " + effect.getSimpleName() + " has caused an exception!", e);
            }
        }
    }

    /**
     * Calculates the opposites of every effect and other misc stuff required to make the mod work
     */
    public static void postInit(){
        Logger logger = ThaumicInfusion.getLogger();
        if(isInCorretState(LoaderState.POSTINITIALIZATION)) {
            logger.warn("Post Init cannot be called outside it's state");
            return;
        }

        for(Class<? extends AspectEffect> effect : effectsToRegister){
            Effect annotation = effect.getAnnotation(Effect.class);
            Aspect aspect = Aspect.getAspect(annotation.aspect().toLowerCase());
            if(aspect != null) {
                if (!registeredEffects.containsKey(aspect)) {
                    registeredEffects.put(aspect, effect);
                    if(annotation.getGUIClass() != EffectGui.class){
                        EffectBundle bundle = new EffectBundle();
                        bundle.guiID = guiEffects.size() + 1;
                        bundle.effect = effect;
                        bundle.gui = annotation.getGUIClass();
                        guiEffects.add(bundle);
                    }
                }
            }else logger.log(Level.ERROR, "Aspect: " + annotation.aspect() + " does not exist in the instance");
        }

        for(Aspect aspect : getRegisteredAspects())
            opposites.put(aspect, calculateEffectOpposites(aspect));


        logger.info(registeredEffects.size() + " effects have been binded to their aspect, Failed to find: " + (effectsToRegister.size() - registeredEffects.size()) + " effects aspects.");
        effectsToRegister = null;
    }

    /**
     * Internal use, calculates incompatible effects to ensure that effects that dont
     * work with each are able to be infused with into a block together
     *
     * @param aspect The Aspect you want to check
     * @return The opposites of the aspect
     */
    private static Aspect[] calculateEffectOpposites(Aspect aspect){
        try {
            ArrayList<Aspect> aspects = new ArrayList<Aspect>();
            AspectEffect effect = getEffectFromAspect(aspect).newInstance();
            for(Aspect checking : getRegisteredAspects()){
                for(Method method : getEffectFromAspect(checking).getDeclaredMethods()) {
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

    /**
     * Internal function to ensure that the methods are called in order
     * @param state The state you want to check
     * @return Whether it is in this current state
     */
    private static boolean isInCorretState(LoaderState state){
        Loader loader = Loader.instance();
        return !loader.isInState(state) && loader.activeModContainer().getModId().matches(ModInfo.MODID);
    }

    public static EffectBundle getGUI(int id){
        for(EffectBundle bundle : guiEffects)
            if(bundle.guiID == id) return bundle;
        return null;
    }

    public static EffectBundle getGUI(Class<? extends AspectEffect> effect){
        for(EffectBundle bundle : guiEffects)
            if(bundle.effect == effect) return bundle;
        return null;
    }

    /**
     * Used to get a list of Aspects that have GUI's
     * @return An Array of {@link Aspect} which have a gui attached to their effect
     */
    public static Aspect[] getGUIAspects(){
        List<Aspect> aspects = new ArrayList<Aspect>();

        for(Aspect aspect : getRegisteredAspects()){
            Class<? extends AspectEffect> effect = getEffectFromAspect(aspect);
            if(effect != null && effect.getAnnotation(Effect.class).getGUIClass() != EffectGui.class) aspects.add(aspect);

        }
        return aspects.toArray(new Aspect[aspects.size()]);
    }

    /**
     * Check if a number of aspect can be infused together
     * @param aspects Aspect you want to check
     * @return Are the aspect compatible with each other
     */
    public static boolean canInfuse(Aspect[] aspects){
        for(Aspect aspect : aspects){
            Aspect[] opposite = opposites.get(aspect);
            if(opposite.length > 0) {
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
     * @param aspect The aspect you want the cost of
     * @return The cost of the aspect
     */
    public static int getCostOfEffect(Aspect aspect){
        Class<? extends AspectEffect> c = getEffectFromAspect(aspect);
        return c == null || c.getAnnotation(Effect.class) == null ? -1 : ((Effect) c.getAnnotation(Effect.class)).cost();
    }

    /**
     * Gets all the registered aspects
     *
     * @return An Array of all the aspects that have been registered with an effect
     */
    public static Aspect[] getRegisteredAspects() {
        Aspect[] aspects = getAllAspects();
        List<Aspect> registeredAspect = new ArrayList<Aspect>();
        for (Aspect aspect : aspects) {
            if (registeredEffects.containsKey(aspect))
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
        List<Aspect> aspects = new ArrayList<Aspect>();
        aspects.addAll(Aspect.getPrimalAspects());
        aspects.addAll(Aspect.getCompoundAspects());
        return aspects.toArray(new Aspect[aspects.size()]);
    }
    /**
     * Converts an AspectEffect Class to its registered Aspect
     * @param effect The class you want the aspect of
     * @return The Aspect registered with the effect
     */
    public static Aspect getAspectsFromEffect(Class effect) {
        if(effect.isAnnotationPresent(Effect.class)){
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
        return registeredEffects.get(aspects);
    }

    public static class EffectBundle {
        public int guiID;
        public Class<? extends EffectGui> gui;
        public Class<? extends AspectEffect> effect;
    }
}

