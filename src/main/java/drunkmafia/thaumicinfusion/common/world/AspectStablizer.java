package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.aspect.effect.vanilla.Praecantatio;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectSource;
import thaumcraft.common.config.ConfigBlocks;

import java.util.concurrent.atomic.AtomicBoolean;

public class AspectStablizer implements Runnable {

    //Tick rate is configurable, this will allow server owners/single-players to mess around with this depending on the performance.
    //A higher tick rate wont cause infusions to degrade faster, it will mean that they are checked more regularly and so will be removed faster.
    private static final int tickRate = 60;
    private static final int jarRadius = 10;
    public static int dataExistedFor = 600;
    private static int tick = AspectStablizer.tickRate;
    private final Logger log = LogManager.getLogger("Aspect Stablizer Thread");
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean die = new AtomicBoolean(false);
    private final WorldServer[] worlds;

    public AspectStablizer(WorldServer[] worlds) {
        this.worlds = worlds;
    }

    @Override
    public void run() {
        this.log.info("--- Thaumic Infusions Thread is Running ---");
        while (!this.die.get()) {
            if (this.running.get()) {
                if (AspectStablizer.tick == 0) {
                    for (WorldServer server : this.worlds) {
                        TIWorldData worldData = TIWorldData.getWorldData(server);
                        if (worldData == null) continue;

                        for (ChunkData chunk : worldData.chunkDatas.getValues()) {
                            if (chunk == null) continue;

                            for (BlockSavable savable : chunk.getAllBlocks()) {
                                if (!(savable instanceof BlockData)) continue;

                                BlockData data = (BlockData) savable;
                                if (data.ticksExisted > AspectStablizer.dataExistedFor) {
                                    worldData.surveyPosition = data.getCoords();
                                    for (AspectEffect effect : data.getEffects()) {
                                        if (!effect.shouldDrain()) continue;

                                        if (!this.drainAspects(server, data.getCoords().x, data.getCoords().y, data.getCoords().z, AspectHandler.getAspectsFromEffect(effect.getClass()))) {
                                            chunk.instability++;
                                            if (chunk.instability > 100)
                                                worldData.world.setBlock(data.getCoords().x, data.getCoords().y, data.getCoords().z, ConfigBlocks.blockFluxGoo);
                                            data.removeEffect(effect.getClass());
                                            if (data.getEffects().length == 0)
                                                worldData.removeData(BlockData.class, data.getCoords(), true);
                                        } else if (chunk.instability > 0) chunk.instability--;
                                    }

                                    worldData.surveyPosition = null;
                                    data.ticksExisted = 0;
                                }
                            }
                        }
                    }
                    AspectStablizer.tick = AspectStablizer.tickRate;
                } else {
                    for (WorldServer server : this.worlds) {
                        TIWorldData worldData = TIWorldData.getWorldData(server);
                        if (worldData == null) continue;

                        for (ChunkData chunk : worldData.chunkDatas.getValues()) {
                            if (chunk == null) continue;

                            for (BlockSavable savable : chunk.getAllBlocks()) {
                                if (savable instanceof BlockData) ((BlockData) savable).ticksExisted++;
                            }
                        }
                    }
                    AspectStablizer.tick--;
                }
                this.running.set(false);
            }
        }
        this.log.info("--- Thaumic Infusions Thread has Stopped ---");
    }

    public boolean drainAspects(World world, int xCoord, int yCoord, int zCoord, Aspect aspect) {
        int cost = AspectHandler.getCostOfEffect(aspect);
        TIWorldData worldData = TIWorldData.getWorldData(world);
        for (int x = xCoord - jarRadius; x < xCoord + jarRadius; x++) {
            for (int y = yCoord - jarRadius; y < yCoord + jarRadius; y++) {
                for (int z = zCoord - jarRadius; z < zCoord + jarRadius; z++) {
                    TileEntity tileEntity = world.getTileEntity(x, y, z);
                    if (tileEntity instanceof IAspectSource) {
                        IAspectSource source = (IAspectSource) tileEntity;
                        BlockData data = worldData.getBlock(BlockData.class, new WorldCoordinates(x, y, z, world.provider.dimensionId));

                        if (data != null && data.hasEffect(Praecantatio.class) && source.doesContainerContainAmount(aspect, cost)) {
                            source.takeFromContainer(aspect, cost);
                            world.playSound((double) ((float) tileEntity.xCoord + 0.5F), (double) ((float) tileEntity.yCoord + 0.5F), (double) ((float) tileEntity.zCoord + 0.5F), "game.neutral.swim", 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F, false);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void setFlags(boolean running, boolean die) {
        this.running.set(running);
        this.die.set(die);
    }
}