package surreal.textiles.tiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.tiles.containers.ContainerSack;
import surreal.textiles.util.TextilesUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileSack extends TileEntity implements ITickable {

    public static int getConfiguredSize() {
        return ModConfig.sack.slotRowCount * ModConfig.sack.slotColumnCount;
    }

    private final SackInventory inventory = new SackInventory(this);
    private int userCount = 0;
    private int localState = 0; // tick timer on server, last known user count on client
    private int dyeColor = -1;
    private int lastSyncDyeColor = -1; // last sent on server, last received on client

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    public boolean isOpen() {
        return userCount > 0;
    }

    public int getDyeColor() {
        return dyeColor;
    }

    public int computeDamageMultiplier() {
        int count = 1;
        final int slotCount = inventory.getSlots();
        for (int i = 0; i < slotCount; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : null;
    }

    public void onOpened(final EntityPlayer player) {
        if (player.isSpectator()) return;
        userCount++;
        world.addBlockEvent(pos, getBlockType(), 1, userCount);
        world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    public void onClosed(final EntityPlayer player) {
        if (userCount == 0 || player.isSpectator()) return;
        userCount--;
        world.addBlockEvent(pos, getBlockType(), 1, userCount);
        world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public boolean receiveClientEvent(final int id, final int type) {
        if (world.isRemote) {
            userCount = type;
        }
        return true;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            if (localState <= 0) {
                if (userCount > 0) {
                    localState = userCount;
                    world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            RegistryManager.SACK_OPEN, SoundCategory.BLOCKS, 0.75F, 1F, false);
                    world.markBlockRangeForRenderUpdate(pos, pos);
                }
            } else if (userCount <= 0) {
                localState = 0;
                world.playSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        RegistryManager.SACK_CLOSE, SoundCategory.BLOCKS, 0.75F, 1F, false);
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        } else {
            localState++;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            if (userCount != 0 && (localState + x + y + z) % 1000 == 0) { // no need for this to update too frequently
                userCount = 0;
                final List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(x - 5D, y - 5D, z - 5D, x + 6D, y + 6D, z + 6D));
                for (final EntityPlayer player : nearbyPlayers) {
                    final Container container = player.openContainer;
                    if (container instanceof ContainerSack sackCont && sackCont.isSameSack(this)) {
                        userCount++;
                    }
                }
            }
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isRemote && dyeColor != lastSyncDyeColor) { // can happen when a falling block lands
            lastSyncDyeColor = dyeColor;
            final IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    public NBTTagCompound writeStateToNBT(final NBTTagCompound tag) {
        tag.setTag("inventory", inventory.serializeNBT());
        if (dyeColor >= 0) {
            tag.setInteger("dye", dyeColor);
        }
        return tag;
    }

    public void readStateFromNBT(final NBTTagCompound tag) {
        inventory.deserializeNBT(tag.getCompoundTag("inventory"));
        dyeColor = tag.hasKey("dye", Constants.NBT.TAG_INT) ? tag.getInteger("dye") : -1;
        if (world != null && world.isRemote && lastSyncDyeColor != dyeColor) {
            lastSyncDyeColor = dyeColor;
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    public NBTTagCompound writeToExternalNBT(final NBTTagCompound tag) {
        writeStateToNBT(tag);
        tag.setString("id", "textiles:sack");
        return tag;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tag) {
        return writeStateToNBT(super.writeToNBT(tag));
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag) {
        super.readFromNBT(tag);
        readStateFromNBT(tag);
    }

    public static class SackInventory extends ItemStackHandler {

        @Nullable
        private final TileEntity hostTile;
        @Nullable
        private final ItemStack hostStack;

        private SackInventory(@Nullable final TileEntity hostTile, @Nullable final ItemStack hostStack) {
            super(getConfiguredSize());
            this.hostTile = hostTile;
            this.hostStack = hostStack;
        }

        public SackInventory(final TileEntity hostTile) {
            this(hostTile, null);
        }

        public SackInventory(final ItemStack hostStack) {
            this(null, hostStack);
        }

        public SackInventory() {
            this(null, null);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
            return !TextilesUtils.isItemWithInventory(stack);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            if (hostTile != null) {
                hostTile.markDirty();
            } else if (hostStack != null) {
                serializeToStack(hostStack);
            }
        }

        @Override
        public void setSize(final int size) { // should only ever be called by deserialization
            if (hostTile == null) return;
            for (int i = size; i < stacks.size(); i++) {
                if (!stacks.get(i).isEmpty()) {
                    final World world = hostTile.getWorld();
                    //noinspection ConstantValue
                    if (world == null) {
                        Textiles.LOGGER.warn(
                                "Sack at {} has lost contents due to lower configured sack inventory size!",
                                hostTile.getPos());
                    } else {
                        Textiles.LOGGER.warn(
                                "Sack in world {} at {} has lost contents due to lower configured sack inventory size!",
                                world.provider.getDimension(), hostTile.getPos());
                    }
                    return;
                }
            }
        }

        public void serializeToStack(final ItemStack stack) {
            NBTTagCompound stackData = stack.getTagCompound();
            final NBTTagCompound tileData;
            if (stackData == null) {
                stackData = new NBTTagCompound();
                tileData = new NBTTagCompound();
                tileData.setString("id", "textiles:sack");
                stackData.setTag("BlockEntityTag", tileData);
                stack.setTagCompound(stackData);
            } else if (stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
                tileData = stackData.getCompoundTag("BlockEntityTag");
            } else {
                tileData = new NBTTagCompound();
                tileData.setString("id", "textiles:sack");
                stackData.setTag("BlockEntityTag", tileData);
            }
            tileData.setTag("inventory", serializeNBT());
        }

        public boolean deserializeFromStack(final ItemStack stack) {
            final NBTTagCompound stackData = stack.getTagCompound();
            if (stackData != null && stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
                final NBTTagCompound tileData = stackData.getCompoundTag("BlockEntityTag");
                if (tileData.hasKey("inventory", Constants.NBT.TAG_COMPOUND)) {
                    deserializeNBT(tileData.getCompoundTag("inventory"));
                    return true;
                }
            }
            return false;
        }

    }

}
