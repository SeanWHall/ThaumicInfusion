package drunkmafia.thaumicinfusion.common.core;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;


/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ClassTransformer implements IClassTransformer {

    public static List<String> blockMethods = new ArrayList<String>();
    private static Logger log = ThaumicInfusion.getLogger();
    private static String[] blacklistMethods = {"getExplosionResistance"};
    private boolean isObf;

    public ClassTransformer() {
        log.info("Class Transformer starting!");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if(bytecode == null)
            return null;

        isObf = !name.equals(transformedName);
        bytecode = injectBlockCode(bytecode);

        return bytecode;
    }

    public byte[] injectBlockCode(byte[] bytecode) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, 0);

        boolean isBlockClass = classNode.name.equals("net/minecraft/block/Block");
        if (classNode.name.equals("drunkmafia/thaumicinfusion/common/aspect/AspectEffect") || !classNode.superName.equals("net/minecraft/block/Block") && !classNode.name.equals("net/minecraft/block/Block"))
            return bytecode;

        int noOfMethods = 0;
        try {
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

                if (Arrays.asList(blacklistMethods).contains(method.name))
                    continue;

                if (isBlockMethod) {
                    Type[] pars = Type.getArgumentTypes(method.desc);
                    WorldParamaters worldPars = getWorldPars(pars);

                    if (worldPars == null)
                        continue;

                    if (isBlockClass)
                        blockMethods.add(method.name);


                    InsnList toInsert = new InsnList();
                    worldPars.loadPars(toInsert);
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "hasWorldData", "(Lnet/minecraft/world/" + (worldPars.isBlockAccess ? "IBlockAccess" : "World") + ";III)Z", false));

                    LabelNode l1 = new LabelNode();
                    toInsert.add(new JumpInsnNode(IFEQ, l1));
                    toInsert.add(new LabelNode());

                    toInsert.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/block/BlockHandler", "block", "Lnet/minecraft/block/Block;"));

                    for (int i = 0; i < pars.length; i++)
                        toInsert.add(new VarInsnNode(pars[i].getOpcode(ILOAD), i + 1));

                    toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/Block", method.name, method.desc, false));
                    toInsert.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
                    toInsert.add(l1);

                    method.instructions.insert(toInsert);
                    noOfMethods++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return bytecode;
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        log.info("Successfully Injected block code into: " + classNode.name + " Injected into: " + noOfMethods);
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
                if (par.getClassName().equals("net.minecraft.world.World") || (worldPars.isBlockAccess = par.getClassName().equals("net.minecraft.world.IBlockAccess")))
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
