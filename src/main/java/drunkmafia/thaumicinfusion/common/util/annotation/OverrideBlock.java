package drunkmafia.thaumicinfusion.common.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OverrideBlock {
    boolean overrideBlockFunc() default true;
    boolean shouldRunAsAir() default false;
}
