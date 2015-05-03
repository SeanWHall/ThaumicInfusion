package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.BlockData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.WorldCoord;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Iterator;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("humanus"), cost = 4)
public class Humanus extends AspectEffect {

    public int rotation;

    @OverrideBlock
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;

        if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(x, z) != BiomeGenBase.hell) {
            EntityPlayer entityplayer1 = null;
            Iterator iterator = world.playerEntities.iterator();
            if(hasFoot(world, x, y, z)) {
                while (iterator.hasNext()) {
                    EntityPlayer entityplayer2 = (EntityPlayer) iterator.next();

                    if (entityplayer2.isPlayerSleeping()) {
                        ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                        if (chunkcoordinates.posX == x && chunkcoordinates.posY == y && chunkcoordinates.posZ == z)
                            entityplayer1 = entityplayer2;
                    }
                }

                if (entityplayer1 != null) {
                    player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied"));
                    return true;
                }

                EntityPlayer.EnumStatus enumstatus = player.sleepInBedAt(x, y, z);

                if (enumstatus == EntityPlayer.EnumStatus.OK) {
                    player.setPositionAndUpdate((double)((float)x + 0.5F), (double)((float)y + 1), (double)((float)z + 0.5F));
                    return true;
                }else {
                    if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW)
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep"));
                    else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE)
                        player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe"));

                    return true;
                }
            }
        }
        return false;
    }

    @OverrideBlock
    public boolean hasFoot(World world, int x, int y, int z){
        for(int i = 0; i < BlockBed.field_149981_a.length; i++){
            int[] coords = BlockBed.field_149981_a[i];
            BlockData blockData = TIWorldData.getWorldData(world).getBlock(BlockData.class, new WorldCoord(x + coords[0], y, z + coords[1]));
            if(blockData != null && blockData.hasEffect(Humanus.class)) {
                if(i != rotation) {
                    rotation = i;
                    ChannelHandler.network.sendToAll(new EffectSyncPacketC(this));
                }
                return true;
            }
        }
        return false;
    }

    @OverrideBlock
    public int getBedDirection(IBlockAccess world, int x, int y, int z) {
        return rotation + 2;
    }

    @OverrideBlock
    public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return true;
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        rotation = tagCompound.getInteger("rotation");
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setInteger("rotation", rotation);
    }
}
