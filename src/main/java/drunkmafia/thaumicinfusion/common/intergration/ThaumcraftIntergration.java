/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.intergration;

import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class ThaumcraftIntergration {

    public static void init() {
        ResearchCategories.registerCategory("THAUMICINFUSION", null, new ResourceLocation(ModInfo.MODID, "textures/research/r_ti.png"), new ResourceLocation(ModInfo.MODID, "textures/research/r_tibg.png"));

        InfusionRecipe infusionRecipe = ThaumcraftApi.addInfusionCraftingRecipe("FOCUSINFUSION", new ItemStack(TIItems.focusInfusing), 4, new AspectList().add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.MIND, 10), new ItemStack(ItemsTC.focusWarding), new ItemStack[]{new ItemStack(ItemsTC.crystalEssence, 3), new ItemStack(ItemsTC.crystalEssence, 3), new ItemStack(Items.quartz), new ItemStack(ItemsTC.shard, 3), new ItemStack(ItemsTC.crystalEssence, 3), new ItemStack(ItemsTC.shard, 3), new ItemStack(Items.quartz), new ItemStack(ItemsTC.shard, 3)});

        new ResearchItem("FOCUSINFUSION", "THAUMICINFUSION", new AspectList().add(Aspect.EARTH, 6).add(Aspect.ORDER, 3).add(Aspect.MIND, 3), -2, 0, 2, new ItemStack(TIItems.focusInfusing)).setPages(new ResearchPage("tc.research_page.FOCUSINFUSION.1"), new ResearchPage(infusionRecipe), new ResearchPage("tc.research_page.FOCUSINFUSION.2"), new ResearchPage("tc.research_page.FOCUSINFUSION.3")).registerResearchItem();
    }
}