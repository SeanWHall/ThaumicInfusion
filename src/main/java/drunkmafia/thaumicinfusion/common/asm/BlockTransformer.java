/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
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

    public static List<BlockMethod> blockMethods = new ArrayList<BlockMethod>();
    public static List<String> bannedSuperClasses = new ArrayList<String>();
    public static List<Interface> blockInterfaces = new ArrayList<Interface>();

    private static int blocksInjected = 0;

    public BlockTransformer() {
        log.info("Block Transformer starting!");

        Interface infusionStabiliser = new Interface("thaumcraft/api/crafting/IInfusionStabiliser");
        infusionStabiliser.addMethod(new IMethod("canStabaliseInfusion", "Z", "L" + world + ";III"));
        blockInterfaces.add(infusionStabiliser);

        bannedSuperClasses.add("drunkmafia/thaumicinfusion/common/aspect/AspectEffect");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (bytecode == null)
            return null;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        boolean isBlockClass = classNode.name.equals(block);
        if (!isBlockClass && (classNode.name.equals("drunkmafia/thaumicinfusion/common/aspect/AspectEffect") || !checkIfisBlock(classNode.superName)))
            return bytecode;

        logger.println("==== " + transformedName + " (" + classNode.name + ") ==== \nFound block Class");

        try {
            int methodNo = 1;
            if (isBlockClass) {
                for (Interface inter : blockInterfaces)
                    inter.injectMethodsIntoClass(classNode);
            }

            for (MethodNode method : classNode.methods) {
                boolean isBlockMethod = isBlockClass;
                if (!isBlockMethod) {
                    for (BlockMethod blockMethod : blockMethods) {
                        if (method.name.equals(blockMethod.methodName) && method.desc.equals(blockMethod.methodDesc)) {
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
                        BlockMethod blockMethod = new BlockMethod();
                        blockMethod.methodName = method.name;
                        blockMethod.methodDesc = method.desc;
                        blockMethods.add(blockMethod);
                        logger.println("Block Method found: " + method.name + " " + method.desc);
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

                    logger.write(methodNo++ + ") Injected block code into: " + method.name + " " + method.desc + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bytecode;
        }
        logger.flush();
        MinecraftClassWriter classWriter = new MinecraftClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        try {
            classNode.accept(classWriter);
        }catch (Throwable t){
            log.info("Block: " + transformedName + "(" + classNode.name + ") has an issue while merging the changes. A detailed crash has been printed to TI_Transformer.log, please upload this log to pastebin and report it to the mod author");
            log.info("Reverting to ordinal bytecode, this block will not be compatible with infusions and will behave abnormally");

            logger.println("==== Block: " + transformedName + " has failed injection ==== ");
            t.printStackTrace(logger);
            return bytecode;
        }
        blocksInjected++;
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

    private boolean checkIfisBlock(String superName){
        try {
            ClassNode superNode = new ClassNode();
            new ClassReader(Launch.classLoader.getClassBytes(superName)).accept(superNode, 0);
            return !bannedSuperClasses.contains(superNode.name) && (superNode.superName != null && superNode.name.equals(block) || !superNode.name.contains("java.lang") && checkIfisBlock(superNode.superName));
        } catch (Throwable e) { }
        return false;
    }

    public static int getBlocksInjected(){
        return blocksInjected;
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

    class BlockMethod {
        String methodName, methodDesc;
    }

    /**
     * Modified version of the ASM Classwriter, which makes it load classes from Minecrafts class loader
     */
    class MinecraftClassWriter extends ClassWriter {

        public MinecraftClassWriter(int flags) {
            super(flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            Class<?> c, d;
            try {
                c = Launch.classLoader.findClass(type1.replace('/', '.'));
                d = Launch.classLoader.findClass(type2.replace('/', '.'));
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }
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
