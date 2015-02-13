package drunkmafia.thaumicinfusion.common.command;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.world.BlockSavable;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class CleanCommand extends CommandBase {

    ArrayList<String> players = new ArrayList<String>();

    @Override
    public String getCommandName() {
        return ThaumicInfusion.translate("clean.data");
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "clean.data.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        World world = sender.getEntityWorld();
        String playerName = sender.getCommandSenderName().toLowerCase();

        if(players.contains(playerName) && strings.length > 0 && (strings[0].toLowerCase().contains("y") || strings[0].toLowerCase().contains("yes"))){
            TIWorldData data = TIWorldData.getWorldData(world);
            BlockSavable[][] dataInWorld = data.getAllStoredData();
            for(BlockSavable[] pos : dataInWorld){
                if(pos.length > 0) {
                    data.removeBlock(pos[0].getCoords(), true);
                }
            }
            sender.addChatMessage(new ChatComponentText("World data has been wiped in dim: " + world.provider.dimensionId));
            players.remove(playerName);
        }else{
            sender.addChatMessage(new ChatComponentText("Are you sure you want to do this? All TI blocks placed down will be removed. Type Y or Yes to continue"));
            players.add(playerName);
        }
    }
}
