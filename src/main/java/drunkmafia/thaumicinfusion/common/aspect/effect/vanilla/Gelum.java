package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "gelum")
public class Gelum extends AspectEffect {

    public static long cooldownTimer = 10000L;
    private final int radius = 10;
    private long cooldown;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @Override
    public int getCost() {
        return 1;
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
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
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote || System.currentTimeMillis() < this.cooldown + Gelum.cooldownTimer)
            return;

        for (int xPos = x - this.radius; xPos < x + this.radius; xPos++) {
            for (int yPos = y - this.radius; yPos < y + this.radius; yPos++) {
                for (int zPos = z - this.radius; zPos < z + this.radius; zPos++) {
                    Block block = world.getBlock(xPos, yPos, zPos);

                    if (block != null) {
                        if (block.getMaterial() == Material.water) {
                            world.setBlock(xPos, yPos, zPos, Blocks.ice);
                            this.cooldown = System.currentTimeMillis();
                        } else if (block != Blocks.snow_layer && world.canBlockSeeTheSky(xPos, yPos, zPos) && world.isAirBlock(xPos, yPos + 1, zPos)) {
                            world.setBlock(xPos, yPos + 1, zPos, Blocks.snow_layer);
                            this.cooldown = System.currentTimeMillis();
                        }
                        return;
                    }
                }
            }
        }
        this.cooldown = System.currentTimeMillis();
    }
}
