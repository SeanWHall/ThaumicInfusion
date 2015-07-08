package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static drunkmafia.thaumicinfusion.common.asm.ThaumicInfusionPlugin.log;
import static org.objectweb.asm.Opcodes.*;

/**
 * This transformer injects the IWorldDataProvider interface into the world class,
 * which contains a getter and setter for a TIWorldData Field, effectively allow TI
 * to store its data in the world class
 */
public class WorldTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String transformedName, String obfName, byte[] bytecode) {
        if (!transformedName.equals("net.minecraft.world.World"))
            return bytecode;

        log.info("Injecting interface into World Class");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        classNode.interfaces.add("drunkmafia/thaumicinfusion/common/world/IWorldDataProvider");
        classNode.fields.add(new FieldNode(ACC_PRIVATE, "worldData", "Ldrunkmafia/thaumicinfusion/common/world/TIWorldData;", null, null));

        //getWorldData Method
        MethodNode getWorldData = new MethodNode(ACC_PUBLIC, "getWorldData", "()Ldrunkmafia/thaumicinfusion/common/world/TIWorldData;", null, null);
        InsnList toInsert = new InsnList();

        toInsert.add(new VarInsnNode(ALOAD, 0));
        toInsert.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", "worldData", "Ldrunkmafia/thaumicinfusion/common/world/TIWorldData;"));
        toInsert.add(new InsnNode(ARETURN));

        getWorldData.instructions.add(toInsert);

        classNode.methods.add(getWorldData);

        //setWorldData Method
        MethodNode setWorldData = new MethodNode(ACC_PUBLIC, "setWorldData", "(Ldrunkmafia/thaumicinfusion/common/world/TIWorldData;)V", null, null);
        toInsert = new InsnList();

        toInsert.add(new VarInsnNode(ALOAD, 0));
        toInsert.add(new VarInsnNode(ALOAD, 1));
        toInsert.add(new FieldInsnNode(PUTFIELD, "net/minecraft/world/World", "worldData", "Ldrunkmafia/thaumicinfusion/common/world/TIWorldData;"));
        toInsert.add(new InsnNode(RETURN));
        setWorldData.instructions.add(toInsert);

        classNode.methods.add(setWorldData);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);

        log.info("Injected interface into World Class");

        return classWriter.toByteArray();
    }
}