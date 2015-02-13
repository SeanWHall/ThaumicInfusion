package drunkmafia.thaumicinfusion.common.util.helper;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.ItemEssence;

import java.util.ArrayList;

public class InfusionHelper {

    public static AspectList getInfusedAspects(ItemStack stack){
        if (!isInfusedStack(stack))
            return null;

        AspectList list = new AspectList();
        Class[] effects = getEffectsFromStack(stack);
        if(effects == null)
            return list;

        for(Class c : effects){
            Effect effect = (Effect) c.getAnnotation(Effect.class);
            list.add(Aspect.getAspect(effect.aspect()), effect.cost());
        }

        return list;
    }

    public static AspectList addBlockAspects(ItemStack stack){
        if(!isInfusedStack(stack))
            return null;
        return new AspectList(new ItemStack(Block.getBlockById(getInfusedID(stack)), 1, stack.getItemDamage()));
    }

    public static boolean isInfusedStack(ItemStack stack){
        if(stack == null || (stack != null && !(Block.getBlockFromItem(stack.getItem()) instanceof InfusedBlock)))
            return false;
        return stack.hasTagCompound();
    }

    public static int getBlockID(Class[] aspects){
        int defBlock = Block.getIdFromBlock(BlockHandler.getBlock("default"));
        for(Class aspect : aspects){
            if(aspect != null && aspect.isAnnotationPresent(Effect.class)) {
                Effect annotation = (Effect) aspect.getAnnotation(Effect.class);
                if(annotation.hasCustomBlock())
                    return Block.getIdFromBlock(BlockHandler.getBlock(annotation.aspect()));
            }
        }
        return defBlock;
    }

    public static int getInfusedID(ItemStack stack){
        if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("InfuseTag"))
            return -1;

        NBTTagCompound tag = stack.stackTagCompound.getCompoundTag("InfuseTag");
        if(tag != null)
            return tag.getInteger("infusedID");
        return -1;
    }

    public static Class[] getEffectsFromStack(ItemStack stack){
        if(stack.stackTagCompound == null)
            return null;

        NBTTagCompound tag = stack.stackTagCompound.getCompoundTag("InfuseTag");
        if(stack.stackTagCompound != null && tag.hasKey("infusedAspect_Size")) {
            Class[] effects = new Class[tag.getInteger("infusedAspect_Size")];
            for (int i = 0; i < effects.length; i++) {
                try {
                    Class c = Class.forName(tag.getString("infusedAspect_" + i));
                    if (c != null) effects[i] = c;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return effects;
        }else return null;
    }

    private static Class[] getEffectsFromList(Aspect[] list) {
        Class[] effects = new Class[list.length];
        for(int i = 0; i < effects.length; i++)
            effects[i] = AspectHandler.getEffectFromAspect(list[i]);

        return effects;
    }

    public static Aspect[] phialsToAspects(ArrayList<ItemStack> stacks){
        ArrayList<Aspect> aspects = new ArrayList<Aspect>();
        for(ItemStack stack : stacks) {
            if (stack.getItem() instanceof ItemEssence) {
                AspectList list = new AspectList();
                list.readFromNBT(stack.getTagCompound());
                aspects.add(list.getAspects()[0]);
            }
        }
        Aspect[] array = new Aspect[aspects.size()];
        array = aspects.toArray(array);
        return array;
    }

    public static BlockData getDataFromStack(ItemStack stack, World world, int x, int y, int z) {
        Class[] classes = getEffectsFromStack(stack);
        if(classes != null) {
            return new BlockData(new WorldCoord(x, y, z, world.provider.dimensionId), classes, getInfusedID(stack), getBlockID(classes));
        }else return null;
    }

    public static ItemStack getInfusedItemStack(Aspect[] list, ItemStack block, int size, int meta){
        if(list == null)
            return null;

        Class[] effects = getEffectsFromList(list);
        if(effects.length == 0)
            return null;

        int blockID = getBlockID(effects), containingId = Block.getIdFromBlock(Block.getBlockFromItem(block.getItem()));
        if(blockID == -1)
            return null;

        ItemStack stack = new ItemStack(Block.getBlockById(blockID), size, meta);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound infuseTag = new NBTTagCompound();

        infuseTag.setInteger("infusedAspect_Size", effects.length);
        infuseTag.setInteger("infusedID", containingId);
        infuseTag.setInteger("infusedMETA", meta);

        for(int i = 0; i < effects.length; i++) {
            if(effects[i] == null) {
                System.out.println("Effects is null");
                return null;
            }
            infuseTag.setString("infusedAspect_" + i, effects[i].getName());
        }

        if(block.stackTagCompound != null)
            tag = block.stackTagCompound;

        tag.setTag("InfuseTag", infuseTag);
        stack.stackTagCompound = tag;

        return stack;
    }
}
