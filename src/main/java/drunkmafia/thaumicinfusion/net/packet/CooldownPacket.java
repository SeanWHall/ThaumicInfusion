package drunkmafia.thaumicinfusion.net.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.lib.ConfigHandler;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;

/**
 * Created by DrunkMafia on 17/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
public abstract class CooldownPacket implements IMessage {

    public static HashMap<WorldCoord, Long> syncTimeouts = new HashMap<WorldCoord, Long>();


    public CooldownPacket() {}

    public boolean canSend(WorldCoord coordinates){
        if (syncTimeouts.containsKey(coordinates)) {
            long timeout = syncTimeouts.get(coordinates);
            if ((timeout + ConfigHandler.maxTimeout) < System.currentTimeMillis()) {
                syncTimeouts.put(coordinates, System.currentTimeMillis());
                return true;
            }
        } else {
            syncTimeouts.put(coordinates, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public abstract void fromBytes(ByteBuf buf);

    @Override
    public abstract void toBytes(ByteBuf buf);
}
