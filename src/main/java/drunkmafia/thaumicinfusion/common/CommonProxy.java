package drunkmafia.thaumicinfusion.common;

import cpw.mods.fml.common.network.IGuiHandler;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.Humanus;
import drunkmafia.thaumicinfusion.common.container.HumanusContainer;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class CommonProxy implements IGuiHandler {

    public void initRenderers() {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) {
            BlockData data = TIWorldData.getData(BlockData.class, world, new WorldCoord(x, y, z));
            if (data != null) {
                Humanus humanus = data.getEffect(Humanus.class);
                if (humanus != null) {
                    EntityPlayer target = humanus.getTarget();
                    if (target != null)
                        return new HumanusContainer(player.inventory, target.inventory);
                }
            }
        }
        return null;
    }
}
