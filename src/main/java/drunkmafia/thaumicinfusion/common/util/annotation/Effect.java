/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.util.annotation;

import drunkmafia.thaumicinfusion.client.gui.aspect.EffectGui;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("ALL")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Effect {
    /**
     * Separate Aspects by using Comma (,)
     */
    String aspect();

    int cost();

    Class<? extends EffectGui> getGUIClass() default EffectGui.class;
}
