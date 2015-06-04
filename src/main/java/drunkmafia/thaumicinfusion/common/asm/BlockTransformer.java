/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import cpw.mods.fml.relauncher.CoreModManager;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


public class BlockTransformer implements IClassTransformer {

    public static List<String> blockMethods = new ArrayList<String>();
    public static boolean isObf;
    public static String block, world, iBlockAccess, getMaterial, chunk;
    public static BufferedWriter logger;
    private static Logger log = LogManager.getLogger("TI Transformer");
    private static String[] blacklistMethods = {"getExplosionResistance"};

    public BlockTransformer() {
        log.info("Class Transformer starting!");
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            isObf = !deobfuscatedEnvironment.getBoolean(null);

            logger = new BufferedWriter(new FileWriter("TI_Transformer_Log.log"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Thaumic Infusion has detected an " + (isObf ? "Obfuscated" : "Deobfuscated") + " environment!");
        block = isObf ? "aji" : "net/minecraft/block/Block";
        world = isObf ? "ahb" : "net/minecraft/world/World";
        iBlockAccess = isObf ? "ahl" : "net/minecraft/world/IBlockAccess";
        getMaterial = isObf ? "func_149688_o" : "getMaterial";
        chunk = isObf ? "apx" : "net/minecraft/world/chunk/Chunk";

        log.info("Block: " + block);
        log.info("World: " + world);
        log.info("Access: " + iBlockAccess);
        log.info(("Chunk: " + chunk));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (bytecode == null)
            return null;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        boolean isBlockClass = classNode.name.equals(block);
        if (classNode.name.equals("drunkmafia/thaumicinfusion/common/aspect/AspectEffect") || !classNode.superName.equals(block) && !classNode.name.equals(block))
            return bytecode;
        try {
            logger.write("==== " + name + " ==== \nFound block Class \n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int methodNo = 1;
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

                if (Arrays.asList(blacklistMethods).contains(method.name))
                    continue;

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

                    toInsert.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "block", "L" + block + ";"));

                    for (int i = 0; i < pars.length; i++)
                        toInsert.add(new VarInsnNode(pars[i].getOpcode(ILOAD), i + 1));

                    toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, block, method.name, method.desc, false));

                    toInsert.add(new InsnNode(returnType));

                    toInsert.add(l2);

                    toInsert.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "block", "L" + block + ";"));

                    for (int i = 0; i < pars.length; i++)
                        toInsert.add(new VarInsnNode(pars[i].getOpcode(ILOAD), i + 1));

                    toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, block, method.name, method.desc, false));
                    if (returnType != RETURN)
                        toInsert.add(new InsnNode(POP));

                    toInsert.add(l1);

                    method.instructions.insert(toInsert);

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

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

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
