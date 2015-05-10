package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by DrunkMafia on 20/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = "machina", cost = 2)
public class Machina extends AspectLink {
    @OverrideBlock
    public int isProvidingWeakPower(IBlockAccess access, int x, int y, int z, int side) {
        World destinationWorld = getDestinationWorld();
        WorldCoord destin = getDestination();
        if (destin == null || destinationWorld.isAirBlock(destin.x, destin.y, destin.z))
            return 0;

        TIWorldData worldData = TIWorldData.getWorldData(destinationWorld);
        BlockData data;
        boolean power = false;
        if ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x - 1, destin.y, destin.z))) == null || !data.hasEffect(Machina.class))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x, destin.y - 1, destin.z, 0) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x + 1, destin.y, destin.z))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x + 1, destin.y, destin.z, 0) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x, destin.y - 1, destin.z))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x, destin.y - 1, destin.z, 0) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x, destin.y + 1, destin.z))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x, destin.y + 1, destin.z, 0) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x, destin.y, destin.z - 1))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x, destin.y, destin.z - 1, 0) > 0;

        if (!power && ((data = worldData.getBlock(BlockData.class, new WorldCoord(destin.x, destin.y, destin.z + 1))) == null || !data.hasEffect(Machina.class)))
            power = destinationWorld.getIndirectPowerLevelTo(destin.x, destin.y, destin.z + 1, 0) > 0;

        return power ? 15 : 0;
    }

    @OverrideBlock
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @OverrideBlock
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @OverrideBlock
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        World destinationWorld = getDestinationWorld();
        WorldCoord destin = getDestination();

        if (pos == null || destinationWorld == null || block.getClass() == getClass())
            return;

        destinationWorld.notifyBlockChange(destin.x, destin.y, destin.z, this);
    }
}
