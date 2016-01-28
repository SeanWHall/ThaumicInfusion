package drunkmafia.thaumicinfusion.client.world;


import net.minecraft.entity.player.EntityPlayer;

public interface IClientRenderer {
    boolean shouldRender(EntityPlayer player);

    void renderData(EntityPlayer player, float partialTicks);
}
