/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.lib.ModInfo;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.common.world.data.BlockData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.EffectSyncPacketC;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.common.Thaumcraft;

@Effect(aspect = "metallum")
public class Metallum extends AspectEffect {

    public Metallum grid[][];

    public boolean isMiddle;

    public Metallum middle;
    public ItemStack item, finalItem;

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        this.onNeighborBlockChange(world, pos.x, pos.y, pos.z, null);
    }

    @Override
    public int getCost() {
        return 4;
    }

    @Override
    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote || this.finalItem == null) return;

        for (Metallum[] row : this.grid) {
            for (Metallum metallum : row) {
                metallum.item = null;
                ChannelHandler.instance().sendToAll(new EffectSyncPacketC(metallum, true));
            }
        }

        this.item = this.finalItem;
        ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
    }

    @Override
    @OverrideBlock
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (this.middle == null || world.isRemote) return true;
        ItemStack equipped = player.getCurrentEquippedItem();

        if (this.item != null) {
            if (equipped != null && this.item.getItem() == equipped.getItem() && this.item.getItemDamage() == equipped.getItemDamage()) {
                if (this.item.stackSize < 64) {
                    this.item.stackSize += equipped.stackSize;
                    if (this.item.stackSize > 64)
                        equipped.stackSize = this.item.stackSize - 64;
                    else equipped.stackSize = 0;
                }

                player.setCurrentItemOrArmor(0, equipped);

                if (this.middle != null) this.middle.itemUpdated(player);
                player.inventory.markDirty();
                ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
                world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
                return true;
            }


            world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, this.item));
            this.item = null;
            ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
            if (this.middle != null) this.middle.itemUpdated(player);
            world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
            return true;
        }

        if (equipped != null) {
            this.item = equipped;
            player.setCurrentItemOrArmor(0, null);

            if (this.middle != null) this.middle.itemUpdated(player);
            player.inventory.markDirty();
            ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));

            world.playSoundEffect(x, y, z, "random.pop", 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);

            return true;
        }

        return false;
    }

    @Override
    @OverrideBlock(overrideBlockFunc = false)
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (world.isRemote) return;

        if (this.middle != null) {
            this.middle.checkGrid(world);
            return;
        }

        TIWorldData worldData = TIWorldData.getWorldData(world);
        for (int xPos = x - 2; xPos < x + 3; xPos++) {
            for (int zPos = z - 2; zPos < z + 3; zPos++) {
                BlockData checkBlock = worldData.getBlock(BlockData.class, new WorldCoordinates(xPos, y, zPos, world.provider.dimensionId));
                if (checkBlock != null && checkBlock.hasEffect(this.getClass())) {
                    Metallum middle = null;
                    Metallum[][] tempGrid = new Metallum[3][3];
                    boolean fail = false;
                    for (int gX = xPos; gX < xPos + 3; gX++) {
                        for (int gZ = zPos; gZ < zPos + 3; gZ++) {
                            checkBlock = worldData.getBlock(BlockData.class, new WorldCoordinates(gX, y, gZ, world.provider.dimensionId));
                            if (checkBlock == null) {
                                fail = true;
                                break;
                            }

                            Metallum checkMetallum = checkBlock.getEffect(this.getClass());
                            if (checkMetallum != null) {
                                if (checkMetallum.grid != null) {
                                    fail = true;
                                    break;
                                }

                                tempGrid[gX - xPos][gZ - zPos] = checkMetallum;
                                if (gX - xPos == 1 && gZ - zPos == 1) middle = checkMetallum;
                            }
                        }
                        if (fail) break;
                    }

                    if (middle == null || fail) continue;

                    middle.setMiddle(tempGrid);
                    return;
                }
            }
        }
    }

    @Override
    public void renderEffect(EntityPlayer player, float partialTicks) {
        double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
        double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        if (this.isMiddle) {
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ModInfo.MODID, "/textures/grid.png"));
            Tessellator tessellator = Tessellator.instance;

            GL11.glPushMatrix();

            GL11.glTranslated(-iPX + this.getPos().x - 1, -iPY + 1.05 + this.getPos().y, -iPZ + this.getPos().z - 1);
            GL11.glRotatef(90, 1, 0, 0);
            GL11.glScalef(3, 3, 1);
            GL11.glEnable(3042);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            tessellator.addVertexWithUV(0.0D, 1.0D, 0.0D, 0.0D, 1.0D);
            tessellator.addVertexWithUV(1.0D, 1.0D, 0.0D, 1.0D, 1.0D);
            tessellator.addVertexWithUV(1.0D, 0.0D, 0.0D, 1.0D, 0.0D);
            tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
            tessellator.draw();
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }

        if (this.item == null) return;

        EntityItem entityitem;

        float ticks = (float) Minecraft.getMinecraft().renderViewEntity.ticksExisted + partialTicks;
        GL11.glPushMatrix();

        GL11.glTranslated(-iPX + this.getPos().x + 0.5F, -iPY + 1.15F + this.getPos().y, -iPZ + this.getPos().z + 0.5F);
        GL11.glRotatef(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
        if (this.item.getItem() instanceof ItemBlock) {
            GL11.glScalef(2.0F, 2.0F, 2.0F);
        } else {
            GL11.glScalef(1.0F, 1.0F, 1.0F);
        }

        ItemStack is = this.item.copy();
        is.stackSize = 1;
        entityitem = new EntityItem(player.worldObj, 0.0D, 0.0D, 0.0D, is);
        entityitem.hoverStart = 0.0F;
        RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        if (!Minecraft.isFancyGraphicsEnabled()) {
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        }
        GL11.glPopMatrix();

        if (this.item != null)
            Thaumcraft.instance.renderEventHandler.drawTextInAir(this.getPos().x, 1.5F + this.getPos().y, this.getPos().z, partialTicks, this.item.stackSize + "");
    }

    private void itemUpdated(EntityPlayer player) {
        Metallum.MetallumContainer container = new Metallum.MetallumContainer(player);
        if (this.grid == null)
            return;

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y)
                container.craftMatrix.setInventorySlotContents(x + y * 3, this.grid[x][y].item);
        }

        this.finalItem = CraftingManager.getInstance().findMatchingRecipe(container.craftMatrix, player.worldObj);
    }

    private void setMiddle(Metallum[][] grid) {
        this.isMiddle = true;

        this.grid = grid;
        for (Metallum[] x : grid) {
            for (Metallum z : x) {
                z.middle = this;
            }
        }
        ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
    }

    private void checkGrid(World world) {
        TIWorldData worldData = TIWorldData.getWorldData(world);
        for (Metallum[] x : this.grid) {
            for (Metallum z : x) {
                BlockData blockData = worldData.getBlock(BlockData.class, z.pos);
                if (blockData == null || !blockData.hasEffect(this.getClass())) {
                    this.removeGrid();
                    return;
                }
            }
        }
    }

    private void removeGrid() {
        for (Metallum[] x : this.grid) {
            for (Metallum z : x) z.middle = null;
        }
        this.isMiddle = false;
        this.grid = null;
        ChannelHandler.instance().sendToAll(new EffectSyncPacketC(this, true));
    }

    @Override
    public void writeNBT(NBTTagCompound tagCompound) {
        super.writeNBT(tagCompound);
        if (this.item != null) {
            NBTTagCompound stackTag = new NBTTagCompound();
            this.item.writeToNBT(stackTag);
            tagCompound.setTag("stackNBT", stackTag);
        }
        tagCompound.setBoolean("isMiddle", this.isMiddle);
    }

    @Override
    public void readNBT(NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        this.isMiddle = tagCompound.getBoolean("isMiddle");
        this.item = tagCompound.hasKey("stackNBT") ? ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("stackNBT")) : null;
    }

    class MetallumContainer extends Container {

        public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
        public IInventory craftResult = new InventoryCraftResult();

        public MetallumContainer(EntityPlayer player) {
            addSlotToContainer(new SlotCrafting(player, craftMatrix, craftResult, 0, 124, 35));

            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 3; ++y)
                    addSlotToContainer(new Slot(craftMatrix, y + x * 3, 30 + y * 18, 17 + x * 18));
            }
        }

        @Override
        public boolean canInteractWith(EntityPlayer player) {
            return true;
        }
    }
}
