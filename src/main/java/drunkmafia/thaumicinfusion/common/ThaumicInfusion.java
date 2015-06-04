/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.Side;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.command.TICommand;
import drunkmafia.thaumicinfusion.common.event.CommonEventContainer;
import drunkmafia.thaumicinfusion.common.intergration.ThaumcraftIntergration;
import drunkmafia.thaumicinfusion.common.item.TIItems;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static drunkmafia.thaumicinfusion.common.lib.ModInfo.*;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies="required-after:Forge@[10.13.2,);required-after:Thaumcraft@[4.2.3.5,)")
public class ThaumicInfusion {

    @Instance(MODID)
    public static ThaumicInfusion instance;

    @SidedProxy(clientSide = CLIENT_PROXY_PATH, serverSide = COMMON_PROXY_PATH)
    public static CommonProxy proxy;

    public Side side = Side.CLIENT;
    public Configuration config;
    public CreativeTabs tab = new CreativeTabs(ModInfo.CREATIVETAB_UNLOCAL) {
        @Override
        public Item getTabIconItem() {
            return TIItems.focusInfusing;
        }
    };

    public static String translate(String key, Object... params) {
        return StatCollector.translateToLocalFormatted(key, params);
    }

    public static Logger getLogger() {
        return LogManager.getLogger(MODID);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        side = event.getSide();
        config = new Configuration(event.getSuggestedConfigurationFile());

        TIItems.init();
        TIBlocks.initBlocks();
        AspectEffect.init();

        FMLInterModComms.sendRuntimeMessage(MODID, "VersionChecker", "addVersionCheck", "https://raw.githubusercontent.com/TheDrunkMafia/ThaumicInfusion/master/version.json");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ChannelHandler.init();
        MinecraftForge.EVENT_BUS.register(new CommonEventContainer());
        proxy.initRenderers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        AspectHandler.postInit();
        ThaumcraftIntergration.init();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
        TICommand.init((ServerCommandManager) event.getServer().getCommandManager());
    }
}
