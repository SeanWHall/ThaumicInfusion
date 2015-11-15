/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import drunkmafia.thaumicinfusion.common.asm.BlockTransformer;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.command.TICommand;
import drunkmafia.thaumicinfusion.common.event.CommonEventContainer;
import drunkmafia.thaumicinfusion.common.intergration.ThaumcraftIntergration;
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.world.AspectStablizer;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import static drunkmafia.thaumicinfusion.common.lib.ModInfo.*;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = "required-after:Forge@[10.13.2,);required-after:Thaumcraft@[4.2.3.5,)")
public class ThaumicInfusion {

    @Mod.Instance(MODID)
    public static ThaumicInfusion instance;
    @SidedProxy(clientSide = CLIENT_PROXY_PATH, serverSide = COMMON_PROXY_PATH)
    public static CommonProxy proxy;
    private static Logger logger;
    public Configuration config;

    public boolean shouldStablizer;
    public AspectStablizer stablizerThread;

    public CreativeTabs tab = new CreativeTabs(CREATIVETAB_UNLOCAL) {
        @Override
        public Item getTabIconItem() {
            return TIItems.focusInfusing;
        }
    };

    public static String translate(String key, Object... params) {
        return StatCollector.translateToLocalFormatted(key, params);
    }

    public static Logger getLogger() {
        return ThaumicInfusion.logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ThaumicInfusion.logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        shouldStablizer = config.get("aspects", "Should Have to Stabilizer?", true).getBoolean();
        config.save();

        TIItems.init();
        TIBlocks.initBlocks();
        AspectEffect.init();

        FMLInterModComms.sendRuntimeMessage(MODID, "VersionChecker", "addVersionCheck", "https://raw.githubusercontent.com/TheDrunkMafia/ThaumicInfusion/master/version.json");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CommonEventContainer());
        FMLCommonHandler.instance().bus().register(new CommonEventContainer());
        NetworkRegistry.INSTANCE.registerGuiHandler(ThaumicInfusion.instance, ThaumicInfusion.proxy);

        ThaumicInfusion.proxy.initRenderers();
        ChannelHandler.registerPackets();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        AspectHandler.postInit();
        ThaumcraftIntergration.init();

        BlockTransformer.blockCheck(Block.blockRegistry.iterator());
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        TICommand.init((ServerCommandManager) server.getCommandManager());

        if (shouldStablizer) {
            this.stablizerThread = new AspectStablizer(server.worldServers);
            new Thread(this.stablizerThread, "Aspect Stabilizer").start();
        }
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        if (stablizerThread != null) {
            this.stablizerThread.setFlags(false, false);
            this.stablizerThread = null;
        }
    }
}
