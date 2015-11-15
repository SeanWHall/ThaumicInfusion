/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLRemappingAdapter;
import net.minecraft.block.Block;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;

import static drunkmafia.thaumicinfusion.common.asm.ThaumicInfusionPlugin.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * This transformer injects code into every single block and the main class itself, the code it injects looks like this:
 * {@code
 * if(BlockWrapper.hasWorldData(world, x, y, z, this, 24)){
 * if(BlockWrapper.overrideBlockFunctionality(world, x, y, z, 24)){
 * return BlockWrapper.block.onBlockActivated(World, x, y, z, player, side, hitX, hitY, hitZ);
 * }else{
 * BlockWrapper.block.onBlockActivated(World, x, y, z, player, side, hitX, hitY, hitZ);
 * }
 * }}
 * <p/>
 * This class is heavily annotated to help debugging if future issues arise & it is essential that this transformer does not conflict with any other mod
 **/
public class BlockTransformer implements IClassTransformer {

    //Interfaces that will be injected into the base block class
    public static List<Interface> blockInterfaces = new ArrayList<Interface>();
    //The Block method which are compatible with the system
    public static List<String> blockMethods = new ArrayList<String>();
    private static boolean shouldInject = true;
    //All the sub classes of the block class that have been found, makes it easier to step though the super classes of the current class being transformed
    private static List<String> blockClasses = new ArrayList<String>();

    private static Map<String, List<String>> injectedClassess = new HashMap<String, List<String>>();
    private static int injectedClasses, totalClasses;

    static {
        Interface infusionStabiliser = new Interface("thaumcraft/api/crafting/IInfusionStabiliser");
        infusionStabiliser.addMethod(new IMethod("canStabaliseInfusion", "Z", "L" + world + ";III"));
        BlockTransformer.blockInterfaces.add(infusionStabiliser);

        BlockTransformer.blockClasses.add("net/minecraft/block/Block");
    }

    public static void blockCheck(Iterator classesIter) {
        while (classesIter.hasNext()) {
            Object obj = classesIter.next();
            if (obj instanceof Block) {
                try {
                    BlockTransformer.searchBlock(Launch.classLoader.getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(obj.getClass().getName()).replace('/', '.')));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        log.info("Thaumic Infusion has finished transforming Block Classes, a total of " + BlockTransformer.injectedClasses + " out of " + BlockTransformer.totalClasses + " have been found & transformed!");
        log.info("Transformer has been disabled, since no more block classes should be getting loaded in!");

        BlockTransformer.shouldInject = false;

        BlockTransformer.injectedClassess = null;
        BlockTransformer.blockClasses = null;
    }

    private static void searchBlock(byte[] bytecode) throws IOException {
        if (bytecode == null) return;
        ClassNode classNode = new ClassNode(ASM5);
        new ClassReader(bytecode).accept(classNode, ClassReader.EXPAND_FRAMES);

        if (classNode.superName == null) return;

        if (!classNode.superName.replace('/', '.').equals(Block.class.getName()))
            BlockTransformer.searchBlock(Launch.classLoader.getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(classNode.superName.replace('.', '/')).replace('/', '.')));

        List<String> methods = BlockTransformer.injectedClassess.get(classNode.name.replace('/', '.'));
        if (methods == null) return;

        BlockTransformer.totalClasses++;

        for (MethodNode method : classNode.methods) {
            if (methods.contains(method.name)) {
                BlockTransformer.injectedClasses++;
                return;
            }
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (bytecode == null || !BlockTransformer.shouldInject)
            return bytecode;

        ClassNode classNode = new ClassNode(ASM5), deobfClassNode = new ClassNode(ASM5);

        //If the instance is obfuscated, then it will run though the deobf transformer to make sure that the src is deobfucated
        new ClassReader(bytecode).accept(classNode, ClassReader.EXPAND_FRAMES);
        this.getDeobfReader(bytecode).accept(deobfClassNode, ClassReader.EXPAND_FRAMES);

        //Uses a custom class writer to load classes from the Vanilla Class loader, to ensure no the classes can be found
        ClassWriter classWriter = new MinecraftClassWriter(classNode.name, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        boolean isBlockClass = deobfClassNode.name.equals("net/minecraft/block/Block");

        if (isBlockClass) {
            log.info("Found the Block Class");
            logger.println("The following log shows the progress of the transformer, any crashes will be logged in here. If you are reporting a crash/bug for TI, please include this log along with the crash!");
            logger.println("==== Transformers ====");
            for (IClassTransformer transformer : Launch.classLoader.getTransformers())
                logger.println("Transformer: " + transformer.getClass().getName());
        }

        //Checks if the ClassNode is the Block class or a subclass
        if (!isBlockClass && !this.checkIfisBlock(deobfClassNode.superName))
            return bytecode;

        boolean hasInjectedCode = false;

        try {
            int methodNo = 1;

            //Injects interfaces required by effects
            if (isBlockClass) {
                for (Interface inter : BlockTransformer.blockInterfaces) {
                    inter.injectMethodsIntoClass(classNode);
                    for (IMethod method : inter.getMethods())
                        BlockTransformer.blockMethods.add(method.getName());
                }
            }

            List<String> methodsInjected = new ArrayList<String>();

            //Iterates though class methods to find block methods and inject code into them
            for (int i = 0; i < classNode.methods.size(); i++) {
                if (i >= deobfClassNode.methods.size()) break;

                MethodNode method = classNode.methods.get(i), deobfMethod = deobfClassNode.methods.get(i);

                // START OF PRE INJECTION CHECKS //

                //Checks to make sure that the method is public or protected & Checks if the method is a block method
                if (method.access != 1 && method.access != 2 || !isBlockClass && !BlockTransformer.blockMethods.contains(deobfMethod.name))
                    continue;

                Type[] pars = Type.getArgumentTypes(method.desc);
                BlockTransformer.WorldParamaters worldPars = this.getWorldPars(pars);

                //Makes sure that the method has a world object and three integers after it which is then inferred as coordinates.
                if (worldPars == null) continue;

                boolean skip = false;

                if (isBlockClass) BlockTransformer.blockMethods.add(deobfMethod.name);
                else {
                    //Check if current method has a super call, this is done to avoid the same method being invoked multiple times.
                    //The method call will be handled to the furthest down super call, which in turn will increase performance
                    for (AbstractInsnNode node : deobfMethod.instructions.toArray()) {
                        if (node instanceof MethodInsnNode) {
                            MethodInsnNode methodIsn = (MethodInsnNode) node;
                            if (methodIsn.name.equals(deobfMethod.name) && methodIsn.owner.equals(deobfClassNode.superName)) {
                                logger.println(methodNo++ + ") Block Method found: " + deobfMethod.name + " (" + deobfMethod.name.hashCode() + ") " + method.desc + " Access: " + method.access + " | SKIPPED (Super call Detected)");
                                skip = true;
                                break;
                            }
                        }
                    }
                }

                if (skip) continue;

                // Sets up the conditional statements
                int returnType = Type.getReturnType(method.desc).getOpcode(IRETURN);

                //Checks to make sure that the methods has not already been injected
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (node != null && node instanceof MethodInsnNode && ((MethodInsnNode) node).owner.equals("drunkmafia/thaumicinfusion/common/block/BlockWrapper")) {
                        logger.println(methodNo++ + ") Block Method found: " + deobfMethod.name + " (" + deobfMethod.name.hashCode() + ") " + method.desc + " Access: " + method.access + " | SKIPPED (Already Injected)");
                        skip = true;
                        break;
                    }
                }

                //Skips the method if it has already been injected into
                if (skip) continue;

                // END OF PRE INJECTION CHECKS  //
                // ---------------------------- //
                // START OF CODE TO BE INJECTED //

                InsnList toInsert = new InsnList();

                //Loads the world object and three integers that the coordinate lookup deems to be the X, Y & Z
                worldPars.loadPars(toInsert);
                //Loads up the Block Object
                toInsert.add(new VarInsnNode(ALOAD, 0));
                //Passes in the method id to make the process of data detection even faster since method lookup is skipped
                //The ID is the methods position in the base Block class, working with ints over strings saves performance and memory
                toInsert.add(new LdcInsnNode(deobfMethod.name.hashCode()));

                toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockWrapper", "hasWorldData", "(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/block/Block;I)Z", false));

                LabelNode hasWorldData = new LabelNode();
                toInsert.add(new JumpInsnNode(IFEQ, hasWorldData));
                toInsert.add(new LabelNode());

                worldPars.loadPars(toInsert);
                toInsert.add(new LdcInsnNode(deobfMethod.name.hashCode()));
                toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockWrapper", "overrideBlockFunctionality", "(Lnet/minecraft/world/IBlockAccess;IIII)Z", false));

                LabelNode overrideBlockFunctionality = new LabelNode();
                toInsert.add(new JumpInsnNode(IFEQ, overrideBlockFunctionality));
                toInsert.add(new LabelNode());

                //Injects Block Invocation Code
                this.injectInvokeBlock(toInsert, method, pars);

                //If override returns true then it skips the blocks code by returning
                toInsert.add(new InsnNode(returnType));

                toInsert.add(overrideBlockFunctionality);

                //If override return false then it runs the effects code and continues with the rest of the method. This is what most effects do, which allows blocks to retain their core functionality
                //Injects Block Invocation Code
                this.injectInvokeBlock(toInsert, method, pars);

                //If the method has a return type, it pops the object off the stack
                if (returnType != RETURN) toInsert.add(new InsnNode(POP));

                toInsert.add(hasWorldData);

                //Adds above code into the method
                method.instructions.insert(toInsert);

                // END OF CODE TO BE INJECTED //

                if (!hasInjectedCode) {
                    logger.println("==== " + transformedName + " (SuperClass: " + classNode.superName + ") ====");
                    hasInjectedCode = true;
                }

                logger.println(methodNo++ + ") Block Method found: " + deobfMethod.name + " (" + deobfMethod.name.hashCode() + ") " + method.desc + " Access: " + method.access + " | INJECTED");
                methodsInjected.add(deobfMethod.name);
            }

            logger.flush();

            //Will only return a modified bytecode if any code has been injected into the methods
            if (hasInjectedCode) {
                classNode.accept(classWriter);
                BlockTransformer.injectedClassess.put(deobfClassNode.name.replace('/', '.'), methodsInjected);

                return classWriter.toByteArray();
            }
        } catch (Throwable t) {
            this.handleCrash(transformedName, t);
        }

        //If no code is injected or an exception is thrown some how, it will revert to the original code
        return bytecode;
    }

    private void handleCrash(String transformedName, Throwable t) {
        log.info("Block: " + transformedName + "has an issue while merging the changes. A detailed crash has been printed to TI_Transformer.log, please upload this log to pastebin and report it to the mod author");
        log.info("Reverting to original bytecode, this block will not be compatible with infusions and will behave abnormally");
        logger.println("==== Block: " + transformedName + " has failed injection ==== ");
        t.printStackTrace(logger);
    }

    /**
     * Returns true if the passed in class is a block class, it checks by stepping though the
     * superclasses until it finds a known class which extends the block class.
     *
     * @param superName Name of the super class that needs to be checked
     * @return true if the class is a Block Subclass
     */
    private boolean checkIfisBlock(String superName) {
        if (superName == null) return false;
        if (BlockTransformer.blockClasses.contains(superName)) return true;

        try {
            byte[] bytecode = Launch.classLoader.getClassBytes(superName.replace('/', '.'));
            if (bytecode == null) {
                if (isObf)
                    bytecode = Launch.classLoader.getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(superName.replace('.', '/')).replace('/', '.'));
                if (bytecode == null) return false;
            }

            ClassReader reader = isObf ? this.getDeobfReader(bytecode) : new ClassReader(bytecode);
            if (this.checkIfisBlock(reader.getSuperName())) {
                logger.println("Found new super: " + superName);
                BlockTransformer.blockClasses.add(superName);
                return true;
            }
        } catch (Throwable ignored) {/* Try 'N Catch is only here as a fail safe, if the code crashes out then the class it is stepping though is not a subclass of the Block */}
        return false;
    }

    /**
     * This method grabs the block object which is set when hasWorldData is called, it then proceeds to invoke the method that is currently being called
     */
    private void injectInvokeBlock(InsnList isnList, MethodNode method, Type[] pars) {
        isnList.add(new FieldInsnNode(GETSTATIC, "drunkmafia/thaumicinfusion/common/block/BlockWrapper", "block", "L" + block + ";"));

        int stackIndex = 1;
        for (Type par : pars) {
            int opcode = par.getOpcode(ILOAD);
            isnList.add(new VarInsnNode(opcode, stackIndex++));
            if (opcode == DLOAD) stackIndex++;
        }

        isnList.add(new MethodInsnNode(INVOKEVIRTUAL, block, method.name, method.desc, false));
    }

    /**
     * Use in obfuscated environments to make it easier to parse though code, this is required because this transformer is loaded
     * before the {@link cpw.mods.fml.common.asm.transformers.DeobfuscationTransformer} which does exactly what this method does
     * but for every class. This transformer is unable to be placed after the deobf transformer, as the FMLPlugin Sorting index will
     * cause the transformer to miss it's chance to inject into the {@link Block}.
     *
     * @param bytecode The bytecode of the class which will be remapped to have deobfucated names
     * @return will return a {@link ClassReader} that contains the modified bytecode
     */
    private ClassReader getDeobfReader(byte[] bytecode) {
        if (!isObf) return new ClassReader(bytecode);
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classReader.accept(new FMLRemappingAdapter(classWriter), ClassReader.EXPAND_FRAMES);
        return new ClassReader(classWriter.toByteArray());
    }

    /**
     * Gets the World and coordinates variables index in the methods stack
     *
     * @param pars The parameters of the method
     * @return a WorldParamaters that is used to load the variables
     */
    public BlockTransformer.WorldParamaters getWorldPars(Type[] pars) {
        BlockTransformer.WorldParamaters worldPars = new BlockTransformer.WorldParamaters();

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
                if (!type1.equals(this.className)) c = Launch.classLoader.findClass(type1.replace('/', '.'));
                if (!type2.equals(this.className)) d = Launch.classLoader.findClass(type2.replace('/', '.'));

                if (c == null && d != null) return d.isInterface() ? "java/lang/Object" : type2;
                if (c != null && d == null) return c.isInterface() ? "java/lang/Object" : type1;
                if (c == null) throw new RuntimeException("Unable to find common super class of " + this.className);
            } catch (Exception e) {
                return null;
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
        boolean isBlockAccess;
        int world = -1, x = -1, y = -1, z = -1;

        public void loadPars(InsnList toInsert) {
            toInsert.add(new VarInsnNode(ALOAD, this.world));
            toInsert.add(new VarInsnNode(ILOAD, this.x));
            toInsert.add(new VarInsnNode(ILOAD, this.y));
            toInsert.add(new VarInsnNode(ILOAD, this.z));
        }
    }
}