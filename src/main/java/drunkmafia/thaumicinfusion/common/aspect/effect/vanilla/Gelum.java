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
    private long cooldown;
    private int radius = 10;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            updateTick(world, pos.x, pos.y, pos.z, world.rand);
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
        if(world.isRemote || System.currentTimeMillis() < cooldown + cooldownTimer)
            return;

        for(int xPos = x - radius; xPos < x + radius; xPos++){
            for(int yPos = y - radius; yPos < y + radius; yPos++){
                for(int zPos = z - radius; zPos < z + radius; zPos++){
                    Block block = world.getBlock(xPos, yPos, zPos);

                    if(block != null) {
                        if(block.getMaterial() == Material.water) {
                            world.setBlock(xPos, yPos, zPos, Blocks.ice);
                            cooldown = System.currentTimeMillis();
                        }else if(block != Blocks.snow_layer && world.canBlockSeeTheSky(xPos, yPos, zPos) && world.isAirBlock(xPos, yPos + 1, zPos)){
                            world.setBlock(xPos, yPos + 1, zPos, Blocks.snow_layer);
                            cooldown = System.currentTimeMillis();
                        }
                        return;
                    }
                }
            }
        }
        cooldown = System.currentTimeMillis();
    }
}
