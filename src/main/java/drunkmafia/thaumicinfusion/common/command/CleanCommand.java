/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.command;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;

public class CleanCommand extends CommandBase {

    ArrayList<String> players = new ArrayList<String>();

    @Override
    public String getName() {
        return ThaumicInfusion.translate("clean.data");
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "clean.data.usage";
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        String playerName = sender.getName().toLowerCase();

        if (this.players.contains(playerName) && args.length > 0 && (args[0].toLowerCase().contains("y") || args[0].toLowerCase().contains("yes"))) {
            TIWorldData data = TIWorldData.getWorldData(world);
            BlockSavable[] savables = data.getAllStoredData();
            for (BlockSavable savable : savables) data.removeData(savable.getClass(), savable.getCoords(), true);

            sender.addChatMessage(new ChatComponentText("World data has been wiped in dim: " + world.provider.getDimensionId()));
            this.players.remove(playerName);
        } else {
            sender.addChatMessage(new ChatComponentText("Are you sure you want to do this? All TI blocks placed down will be removed. Type Y or Yes to continue"));
            this.players.add(playerName);
        }
    }
}
