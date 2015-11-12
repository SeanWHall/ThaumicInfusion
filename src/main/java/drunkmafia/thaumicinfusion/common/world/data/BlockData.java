/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world.data;

import drunkmafia.thaumicinfusion.client.event.ClientEventContainer;
import drunkmafia.thaumicinfusion.common.ThaumicInfusion;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.AspectHandler;
import drunkmafia.thaumicinfusion.common.item.ItemFocusInfusing;
import drunkmafia.thaumicinfusion.common.util.IBlockHook;
import drunkmafia.thaumicinfusion.common.util.IClientTickable;
import drunkmafia.thaumicinfusion.common.util.RGB;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.SavableHelper;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ItemApi;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockData extends BlockSavable implements IBlockHook {

    private static int[] connectedTextureRefByID = new int[]{0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11};
    private static IIcon[] wardedGlassIcon;
    private static HashMap<WorldCoordinates, IIcon> iconCache = new HashMap<WorldCoordinates, IIcon>();

    public World world;
    private int[] methods = new int[0];
    private Map<Integer, OverrideBlock> methodsOverrides = new HashMap<Integer, OverrideBlock>();
    private Map<Integer, Integer> methodsToBlock = new HashMap<Integer, Integer>();
    private ArrayList<AspectEffect> dataEffects = new ArrayList<AspectEffect>();

    public int ticksExisted = 0;

    public BlockData() {}

    public BlockData(WorldCoordinates coords, Class[] list) {
        super(coords);

        for (AspectEffect effect : classesToEffects(list)) {
            if (effect == null) continue;
            effect.data = this;
            dataEffects.add(effect);
        }
    }

    @Override
    public void dataLoad(World world) {
        super.dataLoad(world);

        if(world == null)
            return;

        this.world = world;

        methodsOverrides = new HashMap<Integer, OverrideBlock>();
        methodsToBlock = new HashMap<Integer, Integer>();

        for(int a = 0; a < dataEffects.size(); a++) {
            AspectEffect effect = dataEffects.get(a);
            if (effect == null) {
                ThaumicInfusion.getLogger().error("NULL EFFECT! An effect has been removed or failed to load, the data at: " + getCoords() + " has been removed!");
                TIWorldData.getWorldData(world).removeData(BlockData.class, getCoords(), true);
                return;
            }

            effect.aspectInit(world, getCoords());
            effect.data = this;

            List<AspectEffect.MethodInfo> effectMethods = AspectEffect.getMethods(effect.getClass());
            for (AspectEffect.MethodInfo method : effectMethods) {
                methodsOverrides.put(method.methodID, method.override);
                methodsToBlock.put(method.methodID, dataEffects.indexOf(effect));
            }
        }
        methods = toPrimitive(methodsToBlock.keySet().toArray(new Integer[methodsToBlock.keySet().size()]));
    }

    public void renderData(EntityPlayer player, float partialTicks){
        for(AspectEffect effect : getEffects()) effect.renderEffect(player, partialTicks);

        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem().getClass().isAssignableFrom(ItemApi.getItem("itemWandCasting", 0).getItem().getClass()) && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) != null && ClientEventContainer.getFocus(player.getCurrentEquippedItem()) instanceof ItemFocusInfusing) {
            int x = coordinates.x, y = coordinates.y, z = coordinates.z;

            TIWorldData worldData = TIWorldData.getWorldData(player.getEntityWorld());

            double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
            double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

            for (AspectEffect effect : getEffects()) {
                if (effect instanceof IClientTickable)
                    ((IClientTickable) effect).clientTick(world, (int) -iPX + x, (int) -iPY + y, (int) -iPZ + z, partialTicks);
            }

            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 1);
            GL11.glAlphaFunc(516, 0.003921569F);
            GL11.glTranslated(-iPX + x + 0.5D, -iPY + y, -iPZ + z + 0.5D);

            RenderBlocks renderBlocks = new RenderBlocks();
            GL11.glDisable(2896);
            Tessellator t = Tessellator.instance;
            renderBlocks.setRenderBounds(-0.0010000000474974513D, -0.0010000000474974513D, -0.0010000000474974513D, 1.0010000467300415D, 1.0010000467300415D, 1.0010000467300415D);
            Aspect[] aspects = getAspects();
            if (aspects == null || aspects.length == 0)
                return;

            new RGB(aspects[0].getColor()).glColor3f();

            t.startDrawingQuads();
            t.setBrightness(200);
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            GL11.glTexEnvi(8960, 8704, 260);

            Block blockJar = Block.getBlockFromItem(ItemApi.getBlock("blockJar", 0).getItem());

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[1], y - Facing.offsetsYForSide[1], z - Facing.offsetsZForSide[1]))
                renderBlocks.renderFaceYNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 0, player.ticksExisted));

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[0], y - Facing.offsetsYForSide[0], z - Facing.offsetsZForSide[0]))
                renderBlocks.renderFaceYPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 1, player.ticksExisted));

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[3], y - Facing.offsetsYForSide[3], z - Facing.offsetsZForSide[3]))
                renderBlocks.renderFaceZNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 2, player.ticksExisted));

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[2], y - Facing.offsetsYForSide[2], z - Facing.offsetsZForSide[2]))
                renderBlocks.renderFaceZPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 3, player.ticksExisted));

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[5], y - Facing.offsetsYForSide[5], z - Facing.offsetsZForSide[5]))
                renderBlocks.renderFaceXNeg(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 4, player.ticksExisted));

            if (!isConnectedBlock(worldData, x - Facing.offsetsXForSide[4], y - Facing.offsetsYForSide[4], z - Facing.offsetsZForSide[4]))
                renderBlocks.renderFaceXPos(blockJar, -0.5001D, 0.0D, -0.5001D, this.getIconOnSide(worldData, x, y, z, 5, player.ticksExisted));

            t.draw();
            GL11.glTexEnvi(8960, 8704, 8448);
            GL11.glEnable(2896);
            GL11.glAlphaFunc(516, 0.1F);
            GL11.glDisable(3042);
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }
    }

    private boolean isConnectedBlock(TIWorldData world, int x, int y, int z) {
        BlockData data = world.getBlock(BlockData.class, new WorldCoordinates(x, y, z, 0));
        if (data == null)
            return false;

        int same = 0;
        for (Aspect aspect : data.getAspects()) {
            for (Aspect aspect2 : getAspects()) {
                if (aspect == aspect2) {
                    same++;
                    break;
                }
            }
        }
        return same == data.getAspects().length;
    }

    private IIcon getIconOnSide(TIWorldData world, int x, int y, int z, int side, int ticks) {
        WorldCoordinates wc = new WorldCoordinates(x, y, z, side);
        IIcon out = iconCache.get(wc);
        if ((ticks + side) % 10 == 0 || out == null) {
            boolean[] bitMatrix = new boolean[8];
            if (side == 0 || side == 1) {
                bitMatrix[0] = this.isConnectedBlock(world, x - 1, y, z - 1);
                bitMatrix[1] = this.isConnectedBlock(world, x, y, z - 1);
                bitMatrix[2] = this.isConnectedBlock(world, x + 1, y, z - 1);
                bitMatrix[3] = this.isConnectedBlock(world, x - 1, y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + 1, y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x - 1, y, z + 1);
                bitMatrix[6] = this.isConnectedBlock(world, x, y, z + 1);
                bitMatrix[7] = this.isConnectedBlock(world, x + 1, y, z + 1);
            }

            if (side == 2 || side == 3) {
                bitMatrix[0] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y + 1, z);
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y + 1, z);
                bitMatrix[3] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y, z);
                bitMatrix[4] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y, z);
                bitMatrix[5] = this.isConnectedBlock(world, x + (side == 2 ? 1 : -1), y - 1, z);
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x + (side == 3 ? 1 : -1), y - 1, z);
            }

            if (side == 4 || side == 5) {
                bitMatrix[0] = this.isConnectedBlock(world, x, y + 1, z + (side == 5 ? 1 : -1));
                bitMatrix[1] = this.isConnectedBlock(world, x, y + 1, z);
                bitMatrix[2] = this.isConnectedBlock(world, x, y + 1, z + (side == 4 ? 1 : -1));
                bitMatrix[3] = this.isConnectedBlock(world, x, y, z + (side == 5 ? 1 : -1));
                bitMatrix[4] = this.isConnectedBlock(world, x, y, z + (side == 4 ? 1 : -1));
                bitMatrix[5] = this.isConnectedBlock(world, x, y - 1, z + (side == 5 ? 1 : -1));
                bitMatrix[6] = this.isConnectedBlock(world, x, y - 1, z);
                bitMatrix[7] = this.isConnectedBlock(world, x, y - 1, z + (side == 4 ? 1 : -1));
            }

            int idBuilder = 0;

            for (int i = 0; i <= 7; ++i)
                idBuilder += bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4 : (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32 : (i == 6 ? 64 : 128))))))) : 0;


            if (wardedGlassIcon == null) {
                try {
                    wardedGlassIcon = (IIcon[]) Class.forName("thaumcraft.common.blocks.BlockCosmeticOpaque").getDeclaredField("wardedGlassIcon").get(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            out = (idBuilder <= 255 && idBuilder >= 0) ? wardedGlassIcon[connectedTextureRefByID[idBuilder]] : wardedGlassIcon[0];
            iconCache.put(wc, out);
        }

        return out;
    }

    private static int[] toPrimitive(Integer[] IntegerArray) {
        int[] result = new int[IntegerArray.length];
        for (int i = 0; i < IntegerArray.length; i++) {
            result[i] = IntegerArray[i].intValue();
        }
        return result;
    }

    @Override
    public void setCoords(WorldCoordinates newPos) {
        super.setCoords(newPos);
        for(AspectEffect effect : dataEffects)
            effect.setCoords(newPos);
    }

    public <T extends AspectEffect>T getEffect(Class<T> effect){
        for(AspectEffect obj : dataEffects)
            if(obj.getClass() == effect)
                return effect.cast(obj);
        return null;
    }

    public void removeEffect(Class<? extends AspectEffect> effect) {
        for (AspectEffect aspectEffect : dataEffects) {
            if (!(aspectEffect.getClass() == effect)) continue;
            for (AspectEffect.MethodInfo method : AspectEffect.getMethods(aspectEffect.getClass())) {
                methodsToBlock.remove(method.methodID);
                methodsOverrides.remove(method.methodID);
            }
            dataEffects.remove(aspectEffect);

            if(!world.isRemote)
                ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(this), world.provider.dimensionId);

            return;
        }
    }

    public void addEffect(Class<? extends AspectEffect>[] classes){
        for(AspectEffect effect : classesToEffects(classes)){
            if (effect == null) continue;
            effect.data = this;
            dataEffects.add(effect);
        }

        if(!world.isRemote)
            ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(this), world.provider.dimensionId);

        dataLoad(world);
    }

    public boolean hasEffect(Class<? extends AspectEffect> effect){
        return getEffect(effect) != null;
    }

    private AspectEffect[] classesToEffects(Class[] list) {
        AspectEffect[] effects = new AspectEffect[list.length];
        for (int i = 0; i < effects.length; i++) {
            try {
                AspectEffect eff = (AspectEffect) list[i].newInstance();
                eff.data = this;
                effects[i] = eff;
            }catch (Exception e){}
        }
        return effects;
    }

    public AspectEffect[] getEffects() {
        AspectEffect[] classes = new AspectEffect[dataEffects.size()];
        return dataEffects.toArray(classes);
    }

    public Aspect[] getAspects(){
        AspectEffect[] effects = getEffects();
        Aspect[] aspects = new Aspect[effects.length];
        for(int i = 0; i < effects.length; i++) {
            if(effects[i] == null) continue;
            aspects[i] = AspectHandler.getAspectsFromEffect(effects[i].getClass());
        }

        return aspects;
    }

    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        tagCompound.setInteger("length", dataEffects.size());
        for (int i = 0; i < dataEffects.size(); i++)
            tagCompound.setTag("effect: " + i, SavableHelper.saveDataToNBT(dataEffects.get(i)));
    }

    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        dataEffects = new ArrayList<AspectEffect>();

        for (int i = 0; i < tagCompound.getInteger("length"); i++)
            dataEffects.add((AspectEffect)SavableHelper.loadDataFromNBT(tagCompound.getCompoundTag("effect: " + i)));
    }

    @Override
    public int[] hookMethods(Block block) {
        return methods;
    }

    @Override
    public Block getBlock(int method) {
        Integer index = methodsToBlock.get(method);
        return index != null ? dataEffects.get(index) : null;
    }

    @Override
    public boolean shouldOverride(int method) {
        return methodsOverrides.get(method) != null && methodsOverrides.get(method).overrideBlockFunc();
    }
}
