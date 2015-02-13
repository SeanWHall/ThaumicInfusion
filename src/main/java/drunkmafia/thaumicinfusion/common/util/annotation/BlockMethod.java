package drunkmafia.thaumicinfusion.common.util.annotation;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public @interface BlockMethod {

    Target runType() default Target.EffectOnly;

    public static enum Target{
        EffectOnly(0, -1),
        returnEffect(1, 0),
        Block(0, 1);

        public int effect, block;

        Target(int effect, int block){
            this.effect = effect;
            this.block = block;
        }
    }
}
