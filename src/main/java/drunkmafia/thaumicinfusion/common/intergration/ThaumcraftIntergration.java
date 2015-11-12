/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.intergration;

import drunkmafia.thaumicinfusion.common.block.EssentiaBlock;
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class ThaumcraftIntergration {

    public static void init() {
        ShapedArcaneRecipe essentiaRecipe = null;
        ItemStack essentiaBlock = null;

        for (Aspect aspect : Aspect.aspects.values()) {

            for (int i = 0; i <= 2; i++) {
                ItemStack stack = EssentiaBlock.getEssentiaBlock(aspect, i);

                ItemStack item;
                if (i == 0) {
                    item = ItemApi.getItem("itemEssence", 0);
                    ((IEssentiaContainerItem) item.getItem()).setAspects(item, new AspectList().add(aspect, 8));
                } else if (i == 1) {
                    item = EssentiaBlock.getEssentiaBlock(aspect, 0);
                } else if (i == 2) {
                    item = EssentiaBlock.getEssentiaBlock(aspect, 1);
                } else continue;

                ShapedArcaneRecipe recipe = ThaumcraftApi.addArcaneCraftingRecipe("ESSENTIABLOCKS", stack, new AspectList().add(Aspect.ENTROPY, 4), "PP", "PP", 'P', item);
                if (essentiaRecipe == null)
                    essentiaRecipe = recipe;
                if (essentiaBlock == null)
                    essentiaBlock = stack;
            }
        }

        ResearchCategories.registerCategory("THAUMICINFUSION", new ResourceLocation(ModInfo.MODID, "textures/research/r_ti.png"), new ResourceLocation(ModInfo.MODID, "textures/research/r_tibg.png"));

        InfusionRecipe infusionRecipe = ThaumcraftApi.addInfusionCraftingRecipe("FOCUSINFUSION", new ItemStack(TIItems.focusInfusing), 4, new AspectList().add(Aspect.EARTH, 25).add(Aspect.ARMOR, 25).add(Aspect.ORDER, 25).add(Aspect.MIND, 10), ItemApi.getItem("itemFocusWarding", 0), new ItemStack[]{ItemApi.getItem("itemResource", 3), ItemApi.getItem("itemShard", 3), new ItemStack(Items.quartz), ItemApi.getItem("itemShard", 4), ItemApi.getItem("itemResource", 3), ItemApi.getItem("itemShard", 3), new ItemStack(Items.quartz), ItemApi.getItem("itemShard", 4)});

        new ResearchItem("FOCUSINFUSION", "THAUMICINFUSION", new AspectList().add(Aspect.EARTH, 6).add(Aspect.ARMOR, 3).add(Aspect.ORDER, 3).add(Aspect.MIND, 3), -2, 0, 2, new ItemStack(TIItems.focusInfusing)).setPages(new ResearchPage("tc.research_page.FOCUSINFUSION.1"), new ResearchPage(infusionRecipe), new ResearchPage("tc.research_page.FOCUSINFUSION.2"), new ResearchPage("tc.research_page.FOCUSINFUSION.3")).registerResearchItem();
        new ResearchItem("ESSENTIABLOCKS", "THAUMICINFUSION", new AspectList().add(Aspect.ORDER, 3).add(Aspect.MAGIC, 3), 2, 0, 2, essentiaBlock).setPages(new ResearchPage("tc.research_page.ESSENTIABLOCKS.1"), new ResearchPage(essentiaRecipe)).registerResearchItem();
    }
}