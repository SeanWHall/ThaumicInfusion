/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.command;

import net.minecraft.command.ServerCommandManager;

public class TICommand {
    public static void init(ServerCommandManager manager) {
        manager.registerCommand(new CleanCommand());
    }
}
