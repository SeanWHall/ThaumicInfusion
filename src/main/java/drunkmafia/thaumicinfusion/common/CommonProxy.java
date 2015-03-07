package drunkmafia.thaumicinfusion.common;

import cpw.mods.fml.common.network.IGuiHandler;
import drunkmafia.thaumicinfusion.common.container.FocusInfusionContainer;
import drunkmafia.thaumicinfusion.common.container.InfusedBlockContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {

    public void initRenderers() {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch(ID){
            case 0: return new InfusedBlockContainer();
            case 1:
                return new FocusInfusionContainer(player);
            default: return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {return null;}
}
