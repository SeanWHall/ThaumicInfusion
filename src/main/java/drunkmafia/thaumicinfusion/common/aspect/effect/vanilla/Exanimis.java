/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Effect(aspect = "exanimis")
public class Exanimis extends AspectEffect {

    private final List<String> deadPlayers = new ArrayList<String>();

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @Override
    public int getCost() {
        return 4;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote)
            return;

        for (int i = 0; i < this.deadPlayers.size(); i++) {
            EntityPlayer player = world.getPlayerEntityByName(this.deadPlayers.get(i));
            if (player != null && !player.isDead) {
                this.deadPlayers.remove(i);
                player.setPositionAndUpdate(this.pos.x + 0.5F, this.pos.y + 1F, this.pos.z + 0.5F);
            }
        }

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.pos.x, this.pos.y, this.pos.z, this.pos.x + 1, this.pos.y + 2, this.pos.z + 1);
        ArrayList<EntityPlayer> ents = (ArrayList<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, bb);
        for (EntityPlayer ent : ents)
            if (ent.isDead && !this.deadPlayers.contains(ent.getCommandSenderName()))
                this.deadPlayers.add(ent.getCommandSenderName());
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        return false;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
