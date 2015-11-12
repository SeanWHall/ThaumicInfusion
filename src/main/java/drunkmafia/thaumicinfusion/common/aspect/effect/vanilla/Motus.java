package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.WorldCoordinates;

import java.util.Random;

@Effect(aspect = "motus")
public class Motus extends AspectEffect {

    private final ForgeDirection direction = ForgeDirection.NORTH;
    private long cooldown, maxCooldown = 10000L;
    private int maxSteps = 20;
    private int step;

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public void readConfig(Configuration config) {
        super.readConfig(config);
        this.maxSteps = config.getInt("The amount of blocks that motus is able to push, the larger the amount the more taxing it will be", "Motus", 1, 1, 50, "");
        this.maxCooldown = config.getInt("Cooldown for the effect, stops it from bogging down the CPU if maxing out steps", "Motus", (int) this.maxCooldown, 1000, 1000000, "");
    }

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote)
            return;

        if (System.currentTimeMillis() < this.cooldown) return;

        if (world.blockExists(x, y, z) && world.isBlockIndirectlyGettingPowered(x, y, z)) {
            this.step = 0;
            int airPosition = this.canPushBlock(world, x, y, z, world.getBlock(x, y, z));
            ThaumicInfusion.getLogger().info(airPosition);
            if (airPosition != 0) {
                for (int i = airPosition; i >= 0; i--) {
                    int xCoord = x + i * this.direction.offsetX, yCoord = y + i * this.direction.offsetY, zCoord = z + i * this.direction.offsetZ;
                    Block block = world.getBlock(xCoord, yCoord, zCoord);
                    TileEntity tileEntity = world.getTileEntity(xCoord, yCoord, zCoord);

                    if (block instanceof BlockAir) continue;

                    world.setBlock(xCoord, yCoord, zCoord, Blocks.air);
                    world.setTileEntity(xCoord, yCoord, zCoord, null);

                    world.setBlock(xCoord = x + (i + 1) * this.direction.offsetX, yCoord = y + (i + 1) * this.direction.offsetY, zCoord = z + (i + 1) * this.direction.offsetZ, block);

                    if (tileEntity != null) {
                        tileEntity.xCoord = xCoord;
                        tileEntity.yCoord = yCoord;
                        tileEntity.zCoord = zCoord;
                        world.setTileEntity(xCoord, yCoord, zCoord, tileEntity);
                    }
                }
            }
            this.cooldown = System.currentTimeMillis() + this.maxCooldown;
        }
    }

    private int canPushBlock(World world, int x, int y, int z, Block block) {
        if (block instanceof BlockAir) return this.step;
        this.step++;
        return block.getMaterial().getMaterialMobility() == 0 && this.step != this.maxSteps ? this.canPushBlock(world, x + this.direction.offsetX, y + this.direction.offsetY, z + this.direction.offsetZ, world.getBlock(x + this.direction.offsetX, y + this.direction.offsetY, z + this.direction.offsetZ)) : 0;
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
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
