/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import net.minecraft.block.Block;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLRemappingAdapter;
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
 * }
 * }
 * <p/>
 *
 * However which methods it injects into is another story, the transformer is actively working to try and not inject into a method unless it has to,
 * it steps though the method super calling to make sure it does not run into a method with it already injected into and it also make sure that the
 * method itself is a method that has been marked as compatible during the initial startup
 *
 * This class is heavily annotated to help debugging if future issues arise & it is essential that this transformer does not conflict with any other mod
 **/
public class BlockTransformer implements IClassTransformer {

    //The Block method which are compatible with the system
    public static List<String> blockMethods = new ArrayList<String>();

    private static BlockTransformer instance;
    //All the sub classes of the block class that have been found, makes it easier to step though the super classes of the current class being transformed
    private static List<String> blockClasses = new ArrayList<String>();

    static {
        blockClasses.add("net/minecraft/block/Block");
    }

    private boolean shouldInject = true;
    private Map<String, List<String>> injectedClassess = new HashMap<String, List<String>>();
    private int injectedClasses, totalClasses, injectedMethods, totalMethods;
    private long overallTimeSpent;

    public static void blockCheck(Iterator classesIter) {
        logger.println("==== Failed Blocks ====");

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

        log.info("Thaumic Infusion has finished transforming Block Classes, a total of " + instance.injectedClasses + " out of " + instance.totalClasses + " have been found & transformed!");
        log.info("Also " + instance.injectedMethods + " out of " + instance.totalMethods + " possible methods have had code injected into them!");
        log.info("Total time spent transforming classes: " + instance.overallTimeSpent + " ms");
        log.info("Transformer has been disabled, since no more block classes should be getting loaded in!");

        instance.shouldInject = false;

        instance.totalClasses = 0;
        instance.injectedClasses = 0;
        instance.totalMethods = 0;
        instance.injectedMethods = 0;

        instance.injectedClassess = null;
        blockClasses = null;
    }

    /**
     * Debug method, searchs though a LOADED classes bytecode to see if it's been injected and if everything is fine.
     *
     * @param bytecode Class to search though
     */
    private static void searchBlock(byte[] bytecode) throws IOException {
        if (bytecode == null) return;
        ClassNode classNode = new ClassNode(ASM5);
        new ClassReader(bytecode).accept(classNode, ClassReader.EXPAND_FRAMES);

        if (classNode.superName == null) return;

        if (!classNode.superName.replace('/', '.').equals(Block.class.getName()))
            searchBlock(Launch.classLoader.getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(classNode.superName.replace('.', '/')).replace('/', '.')));

        List<String> methods = instance.injectedClassess.get(classNode.name.replace('/', '.'));
        if (methods == null) return;

        instance.totalClasses++;

        for (MethodNode method : classNode.methods) {
            if (methods.contains(method.name)) {
                instance.injectedClasses++;
                return;
            }
        }

        logger.println("Class: " + classNode.name + " Super: " + classNode.superName);
        logger.flush();
    }

    /**
     * Use in obfuscated environments to make it easier to parse though code, this is required because this transformer is loaded
     * before the {@link net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper} which does exactly what this method does
     * but for every class. This transformer is unable to be placed after the deobf transformer, as the FMLPlugin Sorting index will
     * cause the transformer to miss it's chance to inject into the {@link Block}.
     *
     * @param bytecode The bytecode of the class which will be remapped to have deobfucated names
     * @return will return a {@link ClassReader} that contains the modified bytecode
     */
    public static ClassReader getDeobfReader(byte[] bytecode) {
        if (!isObf) return new ClassReader(bytecode);
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ByteClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        classReader.accept(new FMLRemappingAdapter(classWriter), ClassReader.EXPAND_FRAMES);
        return new ClassReader(classWriter.toByteArray());
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytecode) {
        if (instance == null) instance = this;

        if (bytecode == null || !shouldInject)
            return bytecode;

        long startTime = System.currentTimeMillis();

        ClassNode classNode = new ClassNode(ASM5), deobfClassNode = new ClassNode(ASM5);

        //If the instance is obfuscated, then it will run though the deobf transformer to make sure that the src is deobfucated
        ClassReader reader = new ClassReader(bytecode);
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        ClassReader deobfReader = getDeobfReader(bytecode);
        deobfReader.accept(deobfClassNode, ClassReader.EXPAND_FRAMES);

        ByteClassWriter classWriter = new ByteClassWriter(deobfReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        boolean isBlockClass = deobfClassNode.name.equals("net/minecraft/block/Block");

        String classType;

        //Checks if the ClassNode is the Block class or a subclass
        if (!isBlockClass && ((classType = isClassOfType(deobfClassNode.superName, "net/minecraft/block/Block", classWriter)) == null || !classType.equals(deobfClassNode.superName)))
            return bytecode;

        if (!blockClasses.contains(deobfClassNode.superName)) blockClasses.add(deobfClassNode.superName);

        boolean hasInjectedCode = false;

        try {
            int methodNo = 1;

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

                totalMethods++;

                //At this point, the method is considered a block method and is check further for any duplicate injections or super calls

                boolean skip = false;

                if (isBlockClass) BlockTransformer.blockMethods.add(deobfMethod.name);
                else {
                    //Check if current method has a super call, this is done to avoid the same method being invoked multiple times.
                    //The method call will be handled by the furthest down method in the super call chain, which in turn will increase performance
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

                //Loads the world object and the blockPos
                worldPars.loadPars(toInsert);
                //Loads up the Block Object
                toInsert.add(new VarInsnNode(ALOAD, 0));
                //Passes in the method id to make the process of data detection even faster since method lookup is skipped
                //The ID is the methods position in the base Block class, working with ints over strings saves performance and memory

                //toInsert.add(new LdcInsnNode(deobfMethod.name.hashCode()));
                toInsert.add(new LdcInsnNode(blockMethods.indexOf(deobfMethod.name)));

                toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockWrapper", "hasWorldData", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/Block;I)Z", false));

                LabelNode hasWorldData = new LabelNode();
                toInsert.add(new JumpInsnNode(IFEQ, hasWorldData));
                toInsert.add(new LabelNode());

                worldPars.loadPars(toInsert);

                //toInsert.add(new LdcInsnNode(deobfMethod.name.hashCode()));
                toInsert.add(new LdcInsnNode(blockMethods.indexOf(deobfMethod.name)));

                toInsert.add(new MethodInsnNode(INVOKESTATIC, "drunkmafia/thaumicinfusion/common/block/BlockWrapper", "overrideBlockFunctionality", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;I)Z", false));

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
                    injectedMethods++;
                }

                logger.println(methodNo++ + ") Block Method found: " + deobfMethod.name + " (" + deobfMethod.name.hashCode() + ") " + method.desc + " Access: " + method.access + " | INJECTED");
                methodsInjected.add(deobfMethod.name);
            }

            logger.flush();

            //Will only return a modified bytecode if any code has been injected into the methods
            if (hasInjectedCode) {
                classNode.accept(classWriter);
                injectedClassess.put(deobfClassNode.name.replace('/', '.'), methodsInjected);

                overallTimeSpent += System.currentTimeMillis() - startTime;
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
     * Takes a class and steps though it's super classes to see if it extends the passed in type, avoids
     * using class loader method, to refrain from causing any classes from being loaded
     * @param className The class to step though
     * @param typeName The class to check for
     */
    private String isClassOfType(String className, String typeName, ByteClassWriter classWriter) {
        if (className == null) return null;
        if (className.equals(typeName)) return className;

        try {
            byte[] bytecode = classWriter.getClassBytecode(className.replace('/', '.'));
            if (bytecode == null) {
                if (isObf)
                    bytecode = classWriter.getClassBytecode(FMLDeobfuscatingRemapper.INSTANCE.unmap(className.replace('.', '/')).replace('/', '.'));
                if (bytecode == null) return null;
            }

            ClassReader reader = isObf ? getDeobfReader(bytecode) : new ClassReader(bytecode);
            return isClassOfType(reader.getSuperName(), typeName, classWriter);

        } catch (Throwable t) {
            logger.println("Ran into issues while stepping though Class, Cause: " + className);
            t.printStackTrace(logger);
        }
        return null;
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
                if (par.getClassName().equals(blockPos) || par.getClassName().equals("net.minecraft.util.BlockPos")) {
                    if (worldPars.blockPos == -1) worldPars.blockPos = i + 1;
                    else break;
                }
            } else {
                if (par.getClassName().equals(world.replace("/", ".")) || par.getClassName().equals("net.minecraft.world.World") || (worldPars.isBlockAccess = par.getClassName().equals(iBlockAccess.replace("/", "."))) || (worldPars.isBlockAccess = par.getClassName().equals("net.minecraft.world.IBlockAccess")))
                    worldPars.world = i + 1;
            }
        }

        if (worldPars.world == -1 || worldPars.blockPos == -1)
            return null;

        return worldPars;
    }

    /**
     * Used for storing the indexes of the world and coordinates found in methods
     */
    class WorldParamaters {
        boolean isBlockAccess;
        int world = -1, blockPos = -1;

        public void loadPars(InsnList toInsert) {
            toInsert.add(new VarInsnNode(ALOAD, this.world));
            toInsert.add(new VarInsnNode(ALOAD, this.blockPos));
        }
    }
}