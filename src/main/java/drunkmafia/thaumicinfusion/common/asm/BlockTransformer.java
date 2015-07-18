/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.block.Block;
import net.minecraft.launchwrapper.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 */
public class BlockTransformer implements IClassTransformer {

    private boolean hasBlockLoaded;

    private static Map<String, String> obfClassNames = new HashMap<String, String>();
    private static List<String> blockMethods, vanillaObfMethods = new ArrayList<String>(), blockClasses = new ArrayList<String>();

    public static List<String> bannedSuperClasses = new ArrayList<String>();
    public static List<Interface> blockInterfaces = new ArrayList<Interface>();

    static{
        Interface infusionStabiliser = new Interface("thaumcraft/api/crafting/IInfusionStabiliser");
        infusionStabiliser.addMethod(new IMethod("canStabaliseInfusion", "Z", "L" + world + ";III"));
        blockInterfaces.add(infusionStabiliser);

        blockClasses.add("aji");
        blockClasses.add("net/minecraft/block/Block");

        bannedSuperClasses.add("java/lang/Object");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (bytecode == null)
            return null;

        if(transformedName.equals("net/minecraft/block/Block")) block = name;

        ClassNode classNode = new ClassNode(ASM5);
        ClassReader classReader = new ClassReader(bytecode);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        //Uses a custom class writer to load classes from the Vanilla Class loader, to ensure no the classes can be found
        MinecraftClassWriter classWriter = new MinecraftClassWriter(classNode.name, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        boolean isBlockClass = classNode.name.equals(block);

        //Checks if the ClassNode is the Block class or a subclass
        if(!isBlockClass && (bannedSuperClasses.contains(classNode.superName) || !checkIfisBlock(classNode.superName))) return bytecode;

        //Gets block methods after the block class has already been loaded into the class loader
        if(!isBlockClass && hasBlockLoaded && blockMethods == null && (classNode.superName.equals("net/minecraft/block/Block") || classNode.superName.equals(block))){
            try {
                logger.println("Block class has already been loaded, getting block methods for lookup");
                blockMethods = new ArrayList<String>();
                for (Method method : Block.class.getDeclaredMethods())
                    blockMethods.add(method.getName());
            }catch (Throwable t){
                handleCrash(transformedName, t);
                logger.println("Crash caused by Block class not being loaded before this class");
                return bytecode;
            }
        }else if(!isBlockClass && blockMethods == null) return bytecode;

        boolean hasInjectedCode = false;

        try {
            int methodNo = 1;

            //Injects interfaces required by effects
            if (isBlockClass) {
                for (Interface inter : blockInterfaces)
                    inter.injectMethodsIntoClass(classNode);
            }

            //Iterates though class methods to find block methods and inject code into them
            for (MethodNode method : classNode.methods) {

                //Checks if the method is not static
                if(method.access != 1 && method.access != 2) continue;

                //Checks if the method is a block method
                if(!isBlockClass && !blockMethods.contains(method.name) && !vanillaObfMethods.contains(method.name))
                    continue;

                Type[] pars = Type.getArgumentTypes(method.desc);
                WorldParamaters worldPars = getWorldPars(pars);

                //Makes sure that the method has a world object and three integers after it which is then inferred as coordinates.
                if (worldPars == null)
                    continue;

                //If the class is the Block class, adds the obfuscated method names for other vanilla classes
                if(isBlockClass)
                    vanillaObfMethods.add(method.name);

                // Sets up the conditional statements
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

                //If the method has a return type, it pops the object off the stack
                if (returnType != RETURN) toInsert.add(new InsnNode(POP));

                toInsert.add(l1);

                //Adds above code into the method
                method.instructions.insert(toInsert);

                if(!hasInjectedCode) {
                    logger.println("==== " + transformedName + " (SuperClass: " + classNode.superName + ") ====");
                    hasInjectedCode = true;
                }

                logger.println(methodNo++ + ") Injected block code into: " + method.name + " " + method.desc + " Access: " + method.access);
            }
        } catch (Throwable t) {
            handleCrash(transformedName, t);
            return bytecode;
        }

        logger.flush();

        //If no code has been injected returns original bytecode
        if(!hasInjectedCode)
            return bytecode;

        try {
            classNode.accept(classWriter);
        }catch (Throwable t){
            handleCrash(transformedName, t);
            return bytecode;
        }

        //Adds the de & obfuscated names to a map, which help mod classes find the correct superclass name to load
        if(!name.equals(transformedName)) obfClassNames.put(transformedName.replace('.', '/'), name);

        //Enables the loading of block subclasses
        if(isBlockClass) hasBlockLoaded = true;

        //Returns new bytecode
        return classWriter.toByteArray();
    }

    private void handleCrash(String transformedName, Throwable t){
        log.info("Block: " + transformedName + "has an issue while merging the changes. A detailed crash has been printed to TI_Transformer.log, please upload this log to pastebin and report it to the mod author");
        log.info("Reverting to original bytecode, this block will not be compatible with infusions and will behave abnormally");
        logger.println("==== Block: " + transformedName + " has failed injection ==== ");
        t.printStackTrace(logger);
    }

    /**
     * Returns true if the passed in class is a block class, it checks by stepping though the
     * superclasses until it finds a known class which extends the block class.
     * @param superName Name of the super class that needs to be checked
     * @return true if the class is a Block Subclass
     */
    private boolean checkIfisBlock(String superName){
        if(superName != null && blockClasses.contains(obfClassNames.containsKey(superName.replace('.', '/')) ? obfClassNames.get(superName.replace('.', '/')) : superName)) return true;
        if(superName == null || !hasBlockLoaded || bannedSuperClasses.contains(superName.replace('.', '/'))) return false;

        try {
            ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(superName.replace('/', '.')));
            if(checkIfisBlock(reader.getSuperName())) {
                blockClasses.add(superName);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
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
                if (par.getClassName().equals(world.replace("/", ".")) || par.getClassName().equals("net.minecraft.world.World") || (worldPars.isBlockAccess = par.getClassName().equals(iBlockAccess.replace("/", "."))) || (worldPars.isBlockAccess = par.getClassName().equals("net.minecraft.world.IBlockAccess")))
                    worldPars.world = i + 1;
            }
        }

        if (worldPars.world == -1 || worldPars.x == -1 || worldPars.y == -1 || worldPars.z == -1)
            return null;

        return worldPars;
    }

    /**
     * A modified version of the {@link ClassWriter}, to stop it from trying to load the class its attempting to write.
     */
    class MinecraftClassWriter extends ClassWriter {

        public String className;

        public MinecraftClassWriter(String className, int flags) {
            super(flags);
            this.className = className;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            Class<?> c = null, d = null;

            try {
                if(!type1.equals(className)) c = Launch.classLoader.findClass(type1.replace('/', '.'));
                if(!type2.equals(className)) d = Launch.classLoader.findClass(type2.replace('/', '.'));

                if(c == null && d != null) return d.isInterface() ? "java/lang/Object" : type2;
                if(c != null && d == null) return c.isInterface() ? "java/lang/Object" : type1;
                if(c == null) throw new RuntimeException("Unable to find common super class of " + className);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }

            if (c.isAssignableFrom(d)) return type1;
            if (d.isAssignableFrom(c)) return type2;
            if (c.isInterface() || d.isInterface()) return "java/lang/Object";

            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));

            return c.getName().replace('.', '/');
        }
    }

    /**
     * Used for storing the indexes of the world and coordinates found in methods
     */
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
