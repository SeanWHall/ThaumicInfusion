package drunkmafia.thaumicinfusion.common.block;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.lib.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraftforge.common.config.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static drunkmafia.thaumicinfusion.common.lib.BlockInfo.infusedBlock_UnlocalizedName;

/**
 * Created by DrunkMafia on 04/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class BlockHandler {

    private static ArrayList<String> blockMethods;
    private static ArrayList<String> blacklistedBlocks = new ArrayList<String>();
    private static HashMap<String, InfusedBlock> infusedBlocks = new HashMap<String, InfusedBlock>();

    public static void addBlock(String key, InfusedBlock block){
        if(!infusedBlocks.containsKey(key.toLowerCase()))
            infusedBlocks.put(key.toLowerCase(), block);
    }

    public static InfusedBlock getBlock(String key){
        return infusedBlocks.get(("tile." + infusedBlock_UnlocalizedName + "." + key).toLowerCase());
    }

    public static boolean isBlockMethod(String methName) {
        if(blockMethods == null){
            blockMethods = new ArrayList<String>();
            Method[] methods = InfusedBlock.class.getMethods();
            for(Method method : methods)
                blockMethods.add(method.getName());
        }
        return blockMethods.contains(methName);
    }

    public static void blacklistBlocks(){
        Configuration config = ConfigHandler.config;
        config.load();
        config.addCustomCategoryComment("Blocks", "Blocks that are not allowed to be infused - NOTE: Will use server side config to decide, no conflicts will arise if configs are different");

        Iterator blocksIter = Block.blockRegistry.iterator();
        while (blocksIter.hasNext()) {
            Block block = (Block) blocksIter.next();
            if (config.get("Blocks", block.getLocalizedName(), isTile(block)).getBoolean())
                blacklistedBlocks.add(block.getUnlocalizedName().toLowerCase());
        }

        config.save();
        ThaumicInfusion.instance.logger.info("Blacklisted: " + blacklistedBlocks.size() + " out of " + Block.blockRegistry.getKeys().size());
    }

    public static boolean isBlockBlacklisted(Block block){
        if(block == null)
            return true;

        for(String unlocal : blacklistedBlocks)
            if(unlocal.toLowerCase().matches(block.getUnlocalizedName().toLowerCase()))
                return true;
        return false;
    }

    public static void banBlock(Block block){
        Configuration config = ConfigHandler.config;
        config.load();
        boolean ban = !config.get("Blocks", block.getLocalizedName(), false).getBoolean();
        config.get("Blocks", block.getLocalizedName(), false).set(ban);

        if(!ban)
            blacklistedBlocks.remove(block.getUnlocalizedName().toLowerCase());
        else
            blacklistedBlocks.add(block.getUnlocalizedName().toLowerCase());
        config.save();
    }

    public static boolean isTile(Block block){
        return block instanceof ITileEntityProvider;
    }

    public static boolean hasBlock(String unlocal){
        return infusedBlocks.containsKey(unlocal);
    }

    public static InfusedBlock[] getBlocks(){
        InfusedBlock[] blocks = new InfusedBlock[infusedBlocks.size()];

        Map.Entry[] set = new Map.Entry[blocks.length];
        infusedBlocks.entrySet().toArray(set);

        for(int i = 0; i < set.length; i++)
            blocks[i] = (InfusedBlock) set[i].getValue();

        return blocks;
    }
}
