package drunkmafia.thaumicinfusion.common.command;

import net.minecraft.command.ServerCommandManager;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class TICommand {
    public static void init(ServerCommandManager manager){
        manager.registerCommand(new BanInfusionCommand());
        manager.registerCommand(new CleanCommand());
    }
}
