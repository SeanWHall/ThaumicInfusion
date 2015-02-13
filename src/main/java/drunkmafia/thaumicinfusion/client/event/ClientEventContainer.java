package drunkmafia.thaumicinfusion.client.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.block.InfusedBlock;
import drunkmafia.thaumicinfusion.common.block.TIBlocks;
import drunkmafia.thaumicinfusion.common.lib.BlockInfo;
import drunkmafia.thaumicinfusion.common.util.helper.MathHelper;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by DrunkMafia on 27/06/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@SideOnly(Side.CLIENT)
public class ClientEventContainer {
    @SubscribeEvent
    public void blockHighlight(DrawBlockHighlightEvent event) {
        if (event.target != null) {
            MovingObjectPosition pos = event.target;
            EntityPlayer player = event.player;
            World world = player.worldObj;
            if (world.getBlock(pos.blockX, pos.blockY, pos.blockZ) instanceof InfusedBlock) {
                BlockData blockData = TIWorldData.getData(BlockData.class, world, new WorldCoord(pos.blockX, pos.blockY, pos.blockZ));
                if (blockData != null)
                    for (AspectEffect effect : blockData.runAllAspectMethod())
                        effect.blockHighlight(world, pos.blockX, pos.blockY, pos.blockZ, player, pos, event.partialTicks);
            }
        }
    }
}
