package drunkmafia.thaumicinfusion.common.intergration;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.BlockHandler;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.block.tile.InfusionCoreTile;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.helper.InfusionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemEssence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by DrunkMafia on 08/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class ThaumcraftIntergration {

    public static void init() {
        InfusionRecipe coreRecipe = ThaumcraftApi.addInfusionCraftingRecipe("BLOCKINFUSION", new ItemStack(TIBlocks.infusionCoreBlock), 4, new AspectList().add(Aspect.ORDER, 80).add(Aspect.MAGIC, 40), new ItemStack(ConfigBlocks.blockStoneDevice, 1, 2), new ItemStack[]{new ItemStack(ConfigBlocks.blockCosmeticSolid, 9, 6), new ItemStack(ConfigBlocks.blockCosmeticSolid, 4, 7), new ItemStack(ConfigBlocks.blockCosmeticSolid, 9, 6), new ItemStack(ConfigBlocks.blockCosmeticSolid, 4, 7)});
        ShapedArcaneRecipe essentiaRecipe = null;
        ItemStack essentiaBlock = null;

        for (Aspect aspect : Aspect.aspects.values()) {

            for (int i = 0; i <= 2; i++) {
                ItemStack stack = getEssentiaBlock(aspect, i);

                ItemStack item;
                if (i == 0) {
                    item = new ItemStack(ConfigItems.itemEssence, 1, 1);
                    ((ItemEssence) item.getItem()).setAspects(item, new AspectList().add(aspect, 8));
                } else if (i == 1) {
                    item = getEssentiaBlock(aspect, 0);
                } else if (i == 2) {
                    item = getEssentiaBlock(aspect, 1);
                } else continue;

                ShapedArcaneRecipe recipe = ThaumcraftApi.addArcaneCraftingRecipe("ESSENTIABLOCKS", stack, new AspectList().add(Aspect.ENTROPY, 4), "PP", "PP", Character.valueOf('P'), item);
                if (essentiaRecipe == null)
                    essentiaRecipe = recipe;
                if (essentiaBlock == null)
                    essentiaBlock = stack;
            }
        }

        ResearchCategories.registerCategory("THAUMICINFUSION", new ResourceLocation(ModInfo.MODID, "textures/research/r_ti.png"), new ResourceLocation(ModInfo.MODID, "textures/gui/r_tibg.png"));

        ItemStack empty = new ItemStack(ConfigBlocks.blockHole, 1, 15);
        List core = Arrays.asList(new AspectList().add(Aspect.FIRE, 25).add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.AIR, 25).add(Aspect.ENTROPY, 25).add(Aspect.WATER, 25), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Arrays.asList(empty, null, empty, null, new ItemStack(ConfigBlocks.blockStoneDevice, 1, 2), null, empty, null, empty, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6), null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6), null, null, null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6), null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 6), new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 7), null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 7), null, new ItemStack(TIBlocks.infusionCoreBlock), null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 7), null, new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 7)));

        new ResearchItem("BLOCKINFUSION", "THAUMICINFUSION", new AspectList().add(Aspect.ORDER, 3).add(Aspect.MAGIC, 3), -2, 0, 2, new ItemStack(TIBlocks.infusionCoreBlock)).setPages(new ResearchPage("tc.research_page.BLOCKINFUSION.1"), new ResearchPage(coreRecipe), new ResearchPage("tc.research_page.BLOCKINFUSION.2"), new ResearchPage(core)).registerResearchItem();
        new ResearchItem("ESSENTIABLOCKS", "THAUMICINFUSION", new AspectList().add(Aspect.ORDER, 3).add(Aspect.MAGIC, 3), 2, 0, 2, essentiaBlock).setPages(new ResearchPage("tc.research_page.ESSENTIABLOCKS.1"), new ResearchPage(essentiaRecipe)).registerResearchItem();

        new ResearchItem("ASPECTEFFECTS", "THAUMICINFUSION", new AspectList(), 0, 2, 2, new ResourceLocation("thaumcraft", "textures/misc/r_aspects.png")).setPages(getPages()).setAutoUnlock().registerResearchItem();

        ThaumcraftApi.getCraftingRecipes().add(new BlockInfusionRecipe("", 10));
    }

    static ItemStack getEssentiaBlock(Aspect aspect, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("aspectTag", aspect.getTag());
        ItemStack stack = new ItemStack(TIBlocks.essentiaBlock);
        stack.setItemDamage(meta);
        stack.setTagCompound(tag);
        stack.setStackDisplayName(aspect.getName() + (meta != 0 ? (meta == 1 ? ThaumicInfusion.translate("key.essentiaBlock.brick") : ThaumicInfusion.translate("key.essentiaBlock.chiseled")) : ""));
        return stack;
    }

    private static ResearchPage[] getPages() {
        Aspect[] aspects = AspectHandler.getAspects();
        AspectList current = new AspectList();
        ArrayList<ResearchPage> pages = new ArrayList<ResearchPage>();

        int index = 0;
        for (Aspect aspect : aspects) {
            if (aspect != null) {
                current.add(aspect, AspectHandler.getCostOfEffect(aspect));
                if (index == 1) {
                    pages.add(new AspectEffectPage(current));
                    current = new AspectList();
                    index = 0;
                } else
                    index++;
            }
        }
        ResearchPage[] researchPages = new ResearchPage[pages.size()];
        for (int p = 0; p < researchPages.length; p++)
            researchPages[p] = pages.get(p);

        return researchPages;
    }


    public static class AspectEffectPage extends ResearchPage {

        public AspectList aspects;

        public AspectEffectPage(AspectList aspects) {
            super("");
            this.aspects = aspects;
        }

        @Override
        public String getTranslatedText() {
            String str = "";
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect != null) {
                    ResourceLocation location = aspect.getImage();
                    str += "<IMG>" + location.getResourceDomain() + ":" + location.getResourcePath() + ":0:0:255:255:0.125</IMG>" + aspect.getName() + " Cost: " + AspectHandler.getCostOfEffect(aspect) + " " + ThaumicInfusion.translate("ti.effect_info." + aspect.getName().toUpperCase()) + "\n";
                }
            }
            return str;
        }
    }

    static class BlockInfusionRecipe extends InfusionRecipe {

        public BlockInfusionRecipe(String research, int inst) {
            super(research, null, inst, null, null, null);
        }

        @Override
        public boolean matches(ArrayList<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
            recipeOutput = null;

            boolean isStackSetToInfuse = false;
            for (ItemStack check : InfusionCoreTile.infuseStacksTemp) {
                isStackSetToInfuse = check.getItem() == central.getItem() && check.getItemDamage() == central.getItemDamage() && check.stackSize == central.stackSize;
                if (isStackSetToInfuse)
                    break;
            }

            Block block = (central.getItem() instanceof ItemBlock ? Block.getBlockFromItem(central.getItem()) : null);

            if (block == null || !isStackSetToInfuse || BlockHandler.isBlockBlacklisted(block))
                return false;

            ArrayList<ItemStack> ii = new ArrayList<ItemStack>();
            for (ItemStack is : input)
                if (is.getItem() instanceof ItemEssence) {
                    ii.add(is.copy());
                } else return false;

            AspectList infuseAspects = new AspectList();

            for (ItemStack phial : ii) {
                AspectList phialList = ((ItemEssence) phial.getItem()).getAspects(phial);
                if (phialList == null)
                    return false;
                Aspect aspect = phialList.getAspects()[0];
                if (aspect == null)
                    return false;
                if (infuseAspects.getAmount(aspect) > 0)
                    return false;

                int cost = AspectHandler.getCostOfEffect(aspect);
                if (cost != -1)
                    infuseAspects.add(aspect, cost * central.stackSize);
                else return false;
            }
            if (!AspectHandler.canInfuse(infuseAspects.getAspects()) || !AspectHandler.canInfuse(block, infuseAspects.getAspects()))
                return false;

            aspects = infuseAspects;

            if (ii.size() > 0) {
                try {
                    Field recipeInput = InfusionRecipe.class.getDeclaredField("recipeInput");
                    Field components = InfusionRecipe.class.getDeclaredField("components");

                    recipeInput.setAccessible(true);
                    components.setAccessible(true);

                    recipeInput.set(this, central);

                    ItemStack[] comps = new ItemStack[ii.size()];
                    for (int i = 0; i < comps.length; i++)
                        comps[i] = ii.get(i);

                    components.set(this, comps);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                recipeOutput = InfusionHelper.getInfusedItemStack(InfusionHelper.phialsToAspects(input), new ItemStack(block), central.stackSize, central.getItemDamage());
            }

            return recipeOutput != null && ii.size() > 0;
        }
    }
}