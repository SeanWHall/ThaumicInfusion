package drunkmafia.thaumicinfusion.common.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ASM5;

/**
 * A modified version of the {@link ClassWriter}, Credit: http://www.alia4j.org/alia4j-noirin-core/
 */
public class ClassWriterWithoutClassLoading extends ClassWriter {
    private final Map<String, Set<String>> type2supertypes = new HashMap<String, Set<String>>();
    private final Map<String, String> type2superclass = new HashMap<String, String>();
    private final Map<String, Boolean> type2isInterface = new HashMap<String, Boolean>();

    public ClassWriterWithoutClassLoading(int arg0) {
        super(arg0);
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
        if (this.getSuperTypes(type2).contains(type1)) {
            return type1;
        }
        if (this.getSuperTypes(type1).contains(type2)) {
            return type2;
        }
        if (this.isInterface(type1) || this.isInterface(type2)) {
            return "java/lang/Object";
        } else {
            String type = type1;
            do {
                type = this.getSuperclass(type);
            } while (!this.getSuperTypes(type2).contains(type));
            return type;
        }
    }

    private String getSuperclass(String type) {
        if (!this.type2superclass.containsKey(type)) {
            this.initializeTypeHierarchyFor(type);
        }
        return this.type2superclass.get(type);
    }

    private boolean isInterface(String type) {
        if (!this.type2isInterface.containsKey(type)) {
            this.initializeTypeHierarchyFor(type);
        }
        return this.type2isInterface.get(type);
    }

    private Set<String> getSuperTypes(String type) {
        if (!this.type2supertypes.containsKey(type)) {
            this.initializeTypeHierarchyFor(type);
        }
        return this.type2supertypes.get(type);
    }

    private void initializeTypeHierarchyFor(final String internalTypeName) {
        try {
            ClassReader classReader = new ClassReader(ClassLoader.getSystemResourceAsStream(internalTypeName + ".class"));
            classReader.accept(new ClassVisitor(ASM5) {

                @Override
                public void visit(int version, int access, String name, String signature, String superName,
                                  String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    ClassWriterWithoutClassLoading.this.type2superclass.put(internalTypeName, superName);
                    ClassWriterWithoutClassLoading.this.type2isInterface.put(internalTypeName,
                            (access & Opcodes.ACC_INTERFACE) > 0);
                    Set<String> superTypes = new HashSet<String>();
                    superTypes.add(internalTypeName);
                    if (superName != null) {
                        superTypes.add(superName);
                        superTypes.addAll(ClassWriterWithoutClassLoading.this.getSuperTypes(superName));
                    }
                    for (String superInterface : interfaces) {
                        superTypes.add(superInterface);
                        superTypes.addAll(ClassWriterWithoutClassLoading.this.getSuperTypes(superInterface));
                    }
                    ClassWriterWithoutClassLoading.this.type2supertypes.put(internalTypeName, superTypes);
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
