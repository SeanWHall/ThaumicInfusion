/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static drunkmafia.thaumicinfusion.common.asm.ThaumicInfusionPlugin.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * This transformer injects code into every single block and the main class itself, the code it injects looks like this:
 * <p/>
 * if(BlockHandler.hasWorldData(World, int, int, int, Block){
 *      if(BlockHandler.overrideBlockFunctionality(World, int, int, int)){
 *          return BlockHandler.block.onBlockActivated(World, int, int, int, EntityPlayer, int, float, float, float)
 *      }else{
 *          BlockHandler.block.onBlockActivated(World, int, int, int, EntityPlayer, int, float, float, float)
 *      }
 * }
 * <p/>
 * This code has been optimized to try and negate the performance impact that this causes
 */
public class BlockTransformer implements IClassTransformer {

    public static List<String> blockMethods = new ArrayList<String>();
    public static List<Interface> blockInterfaces = new ArrayList<Interface>();



    public BlockTransformer() {
        log.info("Block Transformer starting!");

        Interface infusionStabiliser = new Interface("thaumcraft/api/crafting/IInfusionStabiliser");
        infusionStabiliser.addMethod(new IMethod("canStabaliseInfusion", "Z", "L" + world + ";III"));
        blockInterfaces.add(infusionStabiliser);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (bytecode == null)
            return null;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        boolean isBlockClass = classNode.name.equals(block);
        if (!isBlockClass && (classNode.name.equals("drunkmafia/thaumicinfusion/common/aspect/AspectEffect") || classNode.superName.equals("java/lang/Object")))
            return bytecode;

        boolean hasCodeBeenInjected = false;


        try {
            int methodNo = 1;
            if (isBlockClass) {
                for (Interface inter : blockInterfaces)
                    inter.injectMethodsIntoClass(classNode);
            }

            for (MethodNode method : classNode.methods) {
                boolean isBlockMethod = isBlockClass;
                if (!isBlockMethod) {
                    for (String blockMethod : blockMethods) {
                        if (method.name.equals(blockMethod)) {
                            isBlockMethod = true;
                            break;
                        }
                    }
                }

                if (method.localVariables == null || method.localVariables.size() == 0) {
                    continue;
                } else if (method.localVariables != null && method.localVariables.size() != 0) {
                    boolean hasThis = false;
                    for (LocalVariableNode varible : method.localVariables) {
                        if (varible.name.equals("this")) {
                            hasThis = true;
                            break;
                        }
                    }
                    if (!hasThis)
                        continue;
                }

                if (isBlockMethod) {
                    Type[] pars = Type.getArgumentTypes(method.desc);
                    WorldParamaters worldPars = getWorldPars(pars);

                    if (worldPars == null)
                        continue;

                    if (isBlockClass) {
                        blockMethods.add(method.name);
                        try {
                            logger.write("Block Method found: " + method.name + " " + method.desc + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!hasCodeBeenInjected) {
                        try {
                            logger.write("==== " + name + " ==== \nFound block Class \n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    int returnType = Type.getReturnType(method.desc).getOpcode(IRETURN);
                    InsnList toInsert = new InsnList();
                    worldPars.loadPars(toInsert);
                    toInsert.add(new VarInsnNode(ALOAD, 0));
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "hasWorldData", "(L" + (worldPars.isBlockAccess ? iBlockAccess : world) + ";IIIL" + block + ";)Z", false));

                    LabelNode l1 = new LabelNode();
                    toInsert.add(new JumpInsnNode(IFEQ, l1));
                    toInsert.add(new LabelNode());

                    worldPars.loadPars(toInsert);
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "overrideBlockFunctionality", "(L" + (worldPars.isBlockAccess ? iBlockAccess : world) + ";III)Z", false));

                    LabelNode l2 = new LabelNode();
                    toInsert.add(new JumpInsnNode(IFEQ, l2));
                    toInsert.add(new LabelNode());

                    //Injects Block Invocation Code
                    injectInvokeBlock(toInsert, method, pars);

                    toInsert.add(new InsnNode(returnType));

                    toInsert.add(l2);

                    //Injects Block Invocation Code
                    injectInvokeBlock(toInsert, method, pars);

                    if (returnType != RETURN)
                        toInsert.add(new InsnNode(POP));

                    toInsert.add(l1);

                    method.instructions.insert(toInsert);
                    hasCodeBeenInjected = true;

                    try {
                        logger.write(methodNo++ + ") Injected block code into: " + method.name + " " + method.desc + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bytecode;
        }

        if (!hasCodeBeenInjected) return bytecode;

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private void injectInvokeBlock(InsnList isnList, MethodNode method, Type[] pars) {
        isnList.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "block", "L" + block + ";"));

        int stackIndex = 1;
        for (Type par : pars) {
            int opcode = par.getOpcode(ILOAD);
            isnList.add(new VarInsnNode(opcode, stackIndex++));
            if (opcode == DLOAD) stackIndex++;
        }

        isnList.add(new MethodInsnNode(INVOKEVIRTUAL, block, method.name, method.desc, false));
    }

    /**
     * Gets the World and coordinates variables index in the methods stack
     *
     * @param pars The parameters of the method
     * @return a WorldParamaters that is used to load the variables
     */
    public WorldParamaters getWorldPars(Type[] pars) {
        WorldParamaters worldPars = new WorldParamaters();

        for (int i = 0; i < pars.length; i++) {
            Type par = pars[i];
            if (worldPars.world != -1) {
                if (par.getClassName().equals("int")) {
                    if (worldPars.x == -1) worldPars.x = i + 1;
                    else if (worldPars.y == -1) worldPars.y = i + 1;
                    else if (worldPars.z == -1) worldPars.z = i + 1;
                    else break;

                } else if (worldPars.x != -1 || worldPars.y != -1 || worldPars.z != -1)
                    break;
            } else {
                if (par.getClassName().equals(world.replace("/", ".")) || (worldPars.isBlockAccess = par.getClassName().equals(iBlockAccess.replace("/", "."))))
                    worldPars.world = i + 1;
            }
        }

        if (worldPars.world == -1 || worldPars.x == -1 || worldPars.y == -1 || worldPars.z == -1)
            return null;

        return worldPars;
    }

    class WorldParamaters {
        boolean isBlockAccess = false;
        int world = -1, x = -1, y = -1, z = -1;

        public void loadPars(InsnList toInsert) {
            toInsert.add(new VarInsnNode(ALOAD, world));
            toInsert.add(new VarInsnNode(ILOAD, x));
            toInsert.add(new VarInsnNode(ILOAD, y));
            toInsert.add(new VarInsnNode(ILOAD, z));
        }
    }
}
