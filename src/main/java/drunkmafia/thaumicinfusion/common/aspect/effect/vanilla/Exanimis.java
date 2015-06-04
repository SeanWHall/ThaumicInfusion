/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Effect(aspect = "exanimis", cost = 4)
public class Exanimis extends AspectEffect {

    private List<String> deadPlayers = new ArrayList<String>();

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if(world.isRemote)
            return;

        for(int i = 0; i < deadPlayers.size(); i++){
            EntityPlayer player = world.getPlayerEntityByName(deadPlayers.get(i));
            if(player != null && !player.isDead){
                deadPlayers.remove(i);
                player.setPositionAndUpdate(pos.x + 0.5F, pos.y + 1F, pos.z + 0.5F);
            }
        }

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 2, pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for(EntityPlayer ent : ents)
            if(ent.isDead && !deadPlayers.contains(ent.getCommandSenderName()))
                deadPlayers.add(ent.getCommandSenderName());
    }
}
