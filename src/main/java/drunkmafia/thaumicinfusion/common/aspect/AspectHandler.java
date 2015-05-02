package drunkmafia.thaumicinfusion.common.aspect;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.GameRegistry;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.lib.ConfigHandler;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AspectHandler {

    private static ArrayList<Class<? extends AspectEffect>> effectsToRegister = new ArrayList<Class<? extends AspectEffect>>();
    private static HashMap<Aspect, Class> registeredEffects = new HashMap<Aspect, Class>();
    private static HashMap<Aspect, Aspect[]> opposites = new HashMap<Aspect, Aspect[]>();

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

                if(effectInstace.shouldRegister()) {
                    GameRegistry.registerBlock(effectInstace, "reg_InfusedBlock" + annotation.aspect());

                    if (!isDef)
                        effectsToRegister.add(effect);
                }
            }catch (Throwable e){
                ThaumicInfusion.getLogger().error("Aspect: " + effect.getSimpleName() + " has caused an exception!", e);
            }
        }
    }

    public static void postInit(){
        Logger logger = ThaumicInfusion.getLogger();
        if(isInCorretState(LoaderState.POSTINITIALIZATION)) {
            logger.warn("Post Init cannot be called outside it's state");
            return;
        }

        Configuration config = ConfigHandler.config;
        config.load();
        config.addCustomCategoryComment("Aspects", "The enabling and disabling of effects - NOTE: These MUST be the same as the server config or MAJOR de-syncs will be caused.");

        for(Class<? extends AspectEffect> effect : effectsToRegister){
            Effect annotation = effect.getAnnotation(Effect.class);
            Aspect aspect = Aspect.getAspect(annotation.aspect().toLowerCase());
            if(aspect != null) {
                if (!registeredEffects.containsKey(aspect) && !registeredEffects.containsValue(effect) && config.get("Aspects", aspect.getName(), true).getBoolean())
                    registeredEffects.put(aspect, effect);
            }else
                logger.log(Level.ERROR, "Aspect: " + annotation.aspect() + " does not exist in the instance");
        }

        for(Aspect aspect : getAspects())
            opposites.put(aspect, calculateEffectOpposites(aspect));


        config.save();
        logger.info(registeredEffects.size() + " effects have been binded to their aspect, Failed to find: " + (effectsToRegister.size() - registeredEffects.size()) + " effects aspects.");
        effectsToRegister = null;
    }

    private static Aspect[] calculateEffectOpposites(Aspect aspect){
        try {
            ArrayList<Aspect> aspects = new ArrayList<Aspect>();
            AspectEffect effect = (AspectEffect) getEffectFromAspect(aspect).newInstance();
            for(Aspect checking : getAspects()){
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

    private static boolean isInCorretState(LoaderState state){
        Loader loader = Loader.instance();
        return !loader.isInState(state) && loader.activeModContainer().getModId().matches(ModInfo.MODID);
    }

    public static Class getEffectFromAspect(Aspect aspects) {
        return registeredEffects.get(aspects);
    }

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

    public static int getCostOfEffect(Aspect aspect){
        Class c = getEffectFromAspect(aspect);
        if(c == null || (c != null && c.getAnnotation(Effect.class) == null))
            return -1;
        Effect annot = (Effect) c.getAnnotation(Effect.class);
        return annot.cost();
    }

    public static Aspect[] getAspects(){
        Map.Entry<Aspect, Class>[] entries = registeredEffects.entrySet().toArray(new Map.Entry[registeredEffects.size()]);
        Aspect[] aspects = new Aspect[entries.length];
        for(int i = 0; i < aspects.length; i++)
            aspects[i] = entries[i].getKey();
        return aspects;
    }

    public static Aspect getAspectsFromEffect(Class effect) {
        if(effect.isAnnotationPresent(Effect.class)){
            Effect annotation = (Effect) effect.getAnnotation(Effect.class);
            return Aspect.getAspect(annotation.aspect());
        }
        return null;
    }
}