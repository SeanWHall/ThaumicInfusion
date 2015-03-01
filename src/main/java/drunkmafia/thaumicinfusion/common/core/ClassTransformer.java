package drunkmafia.thaumicinfusion.common.core;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        boolean isObf = !name.equals(transformedName);

        if(transformedName.equals("net.minecraft.world.World")){
            log.info("Transforming: " + transformedName);
            try{
                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytecode);
                classReader.accept(classNode, 0);

                final String GET_BLOCK = isObf ? "a" : "getBlock";
                final String GET_BLOCK_DESC = isObf ? "(III)Laji;" : "(III)Lnet/minecraft/block/Block;";

                for(MethodNode method : classNode.methods){
                    if(method.name.equals(GET_BLOCK) && method.desc.equals(GET_BLOCK_DESC)){
                        AbstractInsnNode targetNode = null;
                        for (AbstractInsnNode instruction : method.instructions.toArray()) {
                            if (instruction.getOpcode() == ILOAD) {
                                if (((VarInsnNode) instruction).var == 1) {
                                    targetNode = instruction;
                                    break;
                                }
                            }
                        }

                        if (targetNode != null) {
                            log.info("Injecting Code: " + transformedName);
                            AbstractInsnNode popNode = targetNode;
                            for (int i = 0; i < 4; i++)
                                popNode = popNode.getNext();

                            LabelNode newLabelNode = new LabelNode();

                            InsnList condition = new InsnList();
                            condition.add(new VarInsnNode(ALOAD, 1));
                            condition.add(new VarInsnNode(ALOAD, 2));
                            condition.add(new VarInsnNode(ALOAD, 3));
                            condition.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TIWorldData.class), "hasWorldData", isObf ? "(III;)Z" : "(III;)Z", false));
                            condition.add(new JumpInsnNode(IFEQ, newLabelNode));

                            InsnList block = new InsnList();
                            block.add(new VarInsnNode(ALOAD, 1));
                            block.add(new VarInsnNode(ALOAD, 2));
                            block.add(new VarInsnNode(ALOAD, 3));
                            block.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TIWorldData.class), "getBlock", isObf ? "(Laji;)Z" : "(Lnet/minecraft/block/Block;)Z", false));
                            block.add(new InsnNode(ARETURN));

                            method.instructions.insertBefore(targetNode, condition);
                            method.instructions.insert(popNode, newLabelNode);
                        }
                    }
                }

                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            }catch (Exception e){
                log.error("An exception has been thrown while injecting: " + transformedName, e);
            }
        }
        return bytecode;
    }
}
