package drunkmafia.thaumicinfusion.common.core;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ReportedException;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ClassTransformer implements IClassTransformer {

    private static Logger log = ThaumicInfusion.getLogger();

    private boolean isObf;

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if(bytecode == null)
            return null;

        isObf = !name.equals(transformedName);
        log.log(Level.DEBUG, transformedName);
        if(transformedName.equals("net.minecraft.world.World"))
            return transformWorld(bytecode);
        else
            searchForBlockCode(bytecode);

        return bytecode;
    }

    public byte[] transformWorld(byte[] bytecode){
        log.log(Level.DEBUG, "Attempting to inject code into the world class");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        final String GET_BLOCK = isObf ? "a" : "getBlock";
        final String GET_BLOCK_DESC = isObf ? "(III)Laji;" : "(III)Lnet/minecraft/block/Block;";

        for(MethodNode method : classNode.methods){
            if(method.name.equals(GET_BLOCK) && method.desc.equals(GET_BLOCK_DESC)){
                InsnList toInsert = new InsnList();
                toInsert.add(new VarInsnNode(ALOAD, 0));
                toInsert.add(new VarInsnNode(ILOAD, 1));
                toInsert.add(new VarInsnNode(ILOAD, 2));
                toInsert.add(new VarInsnNode(ILOAD, 3));
                toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/world/TIWorldData", "hasWorldData", "(Lnet/minecraft/world/World;III)Z", false));

                LabelNode l1 = new LabelNode();
                toInsert.add(new JumpInsnNode(IFEQ, l1));
                LabelNode l2 = new LabelNode();
                toInsert.add(l2);
                toInsert.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/world/TIWorldData", "block", "Lnet/minecraft/block/Block;"));
                toInsert.add(new InsnNode(ARETURN));
                toInsert.add(l1);
                method.instructions.insert(toInsert);
                break;
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);

        log.log(Level.DEBUG, "Succesfully injected code into the world class");
        return classWriter.toByteArray();
    }

    public void searchForBlockCode(byte[] bytecode){
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        if(classNode.name.equals("drunkmafia/thaumicinfusion/common/block/BlockHandler"))
            return;

        for(MethodNode method : classNode.methods){
            for(AbstractInsnNode node : method.instructions.toArray()){
                if(node instanceof MethodInsnNode){
                    MethodInsnNode methodNode = (MethodInsnNode) node;
                    if(BlockHandler.isBlockMethod(methodNode.name)) {
                        BlockHandler.MethodInfo info = new BlockHandler.MethodInfo();
                        info.methodCallerName = method.name;
                        info.methodName = methodNode.name;
                        BlockHandler.addInvoktion(info);
                    }
                }
            }
        }
    }
}
