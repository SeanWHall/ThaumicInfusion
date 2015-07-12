/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.net.packet.client.ChunkRequestPacketS;
import drunkmafia.thaumicinfusion.net.packet.client.WandAspectPacketS;
import drunkmafia.thaumicinfusion.net.packet.server.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class ChannelHandler extends SimpleNetworkWrapper {

    private static ChannelHandler instance;
    private int ordinal = 0;

    public ChannelHandler(String channelName) {
        super(channelName);

        Side S = Side.SERVER, C = Side.CLIENT;

        //Server Handled Packets
        registerMessage(ChunkRequestPacketS.Handler.class, ChunkRequestPacketS.class, getOrdinal(), S);
        registerMessage(WandAspectPacketS.Handler.class, WandAspectPacketS.class, getOrdinal(), S);

        //Client Handled Packets
        registerMessage(ChunkSyncPacketC.Handler.class, ChunkSyncPacketC.class, getOrdinal(), C);
        registerMessage(BlockSyncPacketC.Handler.class, BlockSyncPacketC.class, getOrdinal(), C);
        registerMessage(EffectSyncPacketC.Handler.class, EffectSyncPacketC.class, getOrdinal(), C);
        registerMessage(EntitySyncPacketC.Handler.class, EntitySyncPacketC.class, getOrdinal(), C);
        registerMessage(DataRemovePacketC.Handler.class, DataRemovePacketC.class, getOrdinal(), C);
    }

    public static ChannelHandler instance() {
        return instance != null ? instance : (instance = new ChannelHandler(ModInfo.CHANNEL));
    }

    @SideOnly(Side.CLIENT)
    public static World getClientWorld(){
        return FMLClientHandler.instance().getClient().theWorld;
    }

    public static WorldServer getServerWorld(int dim){
        return DimensionManager.getWorld(dim);
    }

    private int getOrdinal() {
        return ordinal++;
    }
}
