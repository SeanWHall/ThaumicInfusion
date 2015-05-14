package drunkmafia.thaumicinfusion.common.util.helper;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ReflectionHelper {

    public static Map<Class<?>, Map<Class<?>, Field>> fieldCache = new IdentityHashMap<Class<?>, Map<Class<?>, Field>>();

    public static Field getField(Class type, Class clazz){
        Map fields = fieldCache.get(clazz);
        if (fields != null && fields.containsKey(type))
            return (Field) fields.get(type);

        try{
            for(Field field : clazz.getFields()){
                if(type.isAssignableFrom(field.getType())){
                    field.setAccessible(true);

                    if(fields == null)
                        fields = new IdentityHashMap<Class<?>, Field>();

                    fields.put(type, field);
                    fieldCache.put(clazz, fields);

                    return field;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T>T getObjFromField(Class<T> type, Object clazz){
        Field field = getField(type, clazz.getClass());
        try{
            if(field != null)
                return (T) field.get(clazz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
