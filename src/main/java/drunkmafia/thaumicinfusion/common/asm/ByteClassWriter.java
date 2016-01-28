package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static drunkmafia.thaumicinfusion.common.asm.ThaumicInfusionPlugin.isObf;
import static org.objectweb.asm.Opcodes.ASM5;

/**
 * This class writer is based off:
 * It has been modified to work with Minecrafts class loader and ensures
 * that no classes are loaded during the writing of a class
 */
public class ByteClassWriter extends ClassWriter {

    private static Method readFully;

    static {
        try {
            readFully = LaunchClassLoader.class.getDeclaredMethod("readFully", InputStream.class);
            readFully.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final Map<String, Set<String>> classSuperTypes = new HashMap<String, Set<String>>();
    private final Map<String, String> superClasses = new HashMap<String, String>();
    private final Map<String, Boolean> superInterfaces = new HashMap<String, Boolean>();

    public ByteClassWriter(ClassReader reader, int flags) {
        super(reader, flags);
    }

    /**
     * Returns the common super type of the two given types.
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given classes.
     */
    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        if (checkMap(classSuperTypes, type2, Set.class).contains(type1)) return type1;

        if (checkMap(classSuperTypes, type1, Set.class).contains(type2)) return type2;

        if (checkMap(superInterfaces, type1, Boolean.class) || checkMap(superInterfaces, type2, Boolean.class))
            return "java/lang/Object";

        String type = type1;
        do {
            type = checkMap(superClasses, type, String.class);
        } while (!checkMap(classSuperTypes, type2, Set.class).contains(type));
        return type;
    }

    private <T> T checkMap(Map map, String clazz, Class<T> type) {
        if (!map.containsKey(clazz)) getSupersAndInterfaces(clazz);
        return type.cast(map.get(clazz));
    }

    private void getSupersAndInterfaces(final String clazz) {
        if (clazz == null) return;

        ClassNode classNode = new ClassNode(ASM5);
        InputStream stream = null;
        byte[] bytecode = getClassBytecode(clazz.replace('/', '.'));
        if (bytecode == null) {
            if (isObf)
                bytecode = getClassBytecode(FMLDeobfuscatingRemapper.INSTANCE.unmap(clazz.replace('.', '/')).replace('/', '.'));
            if (bytecode == null) {
                stream = Launch.classLoader.getResourceAsStream(clazz + ".class");
                if (stream == null) return;
            }
        }

        ClassReader classReader = null;
        if (bytecode != null) classReader = new ClassReader(bytecode);
        else {
            try {
                classReader = new ClassReader(stream);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        if (classReader == null) {
            ThaumicInfusionPlugin.log.error("Failed to get class: " + clazz + " expect a failure of classwriting");
            return;
        }

        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        superClasses.put(clazz, classNode.superName);
        superInterfaces.put(clazz, (classNode.access & Opcodes.ACC_INTERFACE) > 0);

        Set<String> superTypes = new HashSet<String>();
        superTypes.add(clazz);
        if (classNode.superName != null) {
            superTypes.add(classNode.superName);
            Set<String> types = checkMap(classSuperTypes, classNode.superName, Set.class);
            if (types != null)
                superTypes.addAll(types);
        }

        for (String superInterface : classNode.interfaces) {
            superTypes.add(superInterface);
            Set<String> types = checkMap(classSuperTypes, superInterface, Set.class);
            if (types != null)
                superTypes.addAll(types);
        }

        classSuperTypes.put(clazz, superTypes);

        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getClassBytecode(String name) {
        try {
            LaunchClassLoader loader = Launch.classLoader;
            InputStream stream = null;
            try {
                name = name.replace('.', '/').concat(".class");
                URL classURL = loader.findResource(name);
                if (classURL == null) return null;

                stream = classURL.openStream();
                return (byte[]) readFully.invoke(loader, stream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return null;
    }
}
