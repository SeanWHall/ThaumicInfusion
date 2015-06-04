/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.client.renderer.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.client.ClientProxy;
import drunkmafia.thaumicinfusion.common.block.tile.InscriberTile;
import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.codechicken.lib.math.MathHelper;
import thaumcraft.codechicken.lib.vec.Rotation;
import thaumcraft.codechicken.lib.vec.Scale;
import thaumcraft.codechicken.lib.vec.Translation;
import thaumcraft.codechicken.lib.vec.Vector3;

import static drunkmafia.thaumicinfusion.client.renderer.RenderingHelper.*;

@SideOnly(Side.CLIENT)
public class InscriberTileRenderer extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float deltaTime) {
        InscriberTile inscriberTile = (InscriberTile) tile;
        ItemStack stack = inscriberTile.getStackInSlot(0);

        float targetRotTop;
        float targetHover = 0;

        if (stack != null) {
            targetRotTop = 90;
            if (inscriberTile.topRotation > 45) {
                targetHover = 0.5F;
                if (inscriberTile.spin > 359)
                    inscriberTile.spin = 0;

                inscriberTile.spin += 2F * deltaTime;
            }
        } else {
            targetHover = 0;
            if (inscriberTile.hover < 0.1F)
                targetRotTop = 0;
            else targetRotTop = 90;
        }

        inscriberTile.topRotation = MathHelper.interpolate(inscriberTile.topRotation, targetRotTop, 0.1F * deltaTime);
        inscriberTile.hover = MathHelper.interpolate(inscriberTile.hover, targetHover, 0.05F * deltaTime);

        ClientProxy proxy = ClientProxy.getInstance();

        //Renders Base
        renderSimpleModel(proxy.inscrber, "Base_Cube", BlockInfo.inscriber_Texture, new Translation(x + 0.5D, y, z + 0.5D), new Scale(1, 1, 1));

        //Renders Lid
        renderSimpleModel(proxy.inscrber, "Lid_Cube.002", BlockInfo.inscriber_Texture, new Translation(x, y + 0.25D, z + 0.5D), new Scale(1, 1, 1), new Rotation(inscriberTile.topRotation, 0, 0, 1));
        renderSimpleModel(proxy.inscrber, "Lid_Cube.002", BlockInfo.inscriber_Texture, new Translation(x + 1D, y + 0.25D, z + 0.5D), new Scale(1, 1, 1), new Rotation(180, 0, 1, 0), new Rotation(inscriberTile.topRotation, 0, 0, 1));

        //Renders Cores
        renderSimpleModel(proxy.core, BlockInfo.infusionCore_Texture, new Translation(x + 0.5F, y + 0.2D + inscriberTile.hover, z + 0.5F), new Scale(1, 1.3, 1), stack != null ? new Rotation(inscriberTile.spin, 1, 1, 1) : new Rotation(0, new Vector3()));
        renderSimpleModel(proxy.core, BlockInfo.infusionCore_Texture, new Translation(x + 0.5F, y + 0.2D + inscriberTile.hover, z + 0.5F), new Scale(0.7, 1.3, 0.7), stack != null ? new Rotation(-inscriberTile.spin, 1, 1, 1) : new Rotation(25, new Vector3(0, 1, 0)));

        //Renders Liquid
        renderLiquid(inscriberTile.aspect, x + 0.5, y + 0.35, z + 0.5);

        //Renders Inventory
        renderInventory(stack, new Translation(x + 0.5F, y + 0.15 + inscriberTile.hover, z + 0.5F), 1F);

    }
}
