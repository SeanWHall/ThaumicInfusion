package drunkmafia.thaumicinfusion.common.util;

import net.minecraft.world.World;

public interface IClientTickable {
    void clientTick(World world, int x, int y, int z, float partialTickTime);
}
