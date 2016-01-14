package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IClientTickable {
    void clientTick(World world, BlockPos pos, float partialTickTime);
}
