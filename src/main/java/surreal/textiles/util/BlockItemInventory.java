package surreal.textiles.util;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import surreal.textiles.Textiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockItemInventory extends ItemStackHandler implements ICapabilityProvider {

    private final String tileEntityId;
    @Nullable
    private final Host host;

    public BlockItemInventory(final int size, final String tileEntityId, @Nullable final Host host) {
        super(size);
        this.tileEntityId = tileEntityId;
        this.host = host;
    }

    public BlockItemInventory(final int size, final String tileEntityId, final ItemStack hostStack,
                              final boolean doRefresh) {
        this(size, tileEntityId, new Host.Stack(hostStack, doRefresh));
    }

    public BlockItemInventory(final int size, final String tileEntityId, final TileEntity hostTile) {
        this(size, tileEntityId, new Host.Tile(hostTile));
    }

    public BlockItemInventory(final int size, final String tileEntityId) {
        this(size, tileEntityId, (Host) null);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? null
                : CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
    }

    @Override
    public void setStackInSlot(final int slot, @Nonnull final ItemStack stack) {
        if (host != null) {
            host.refresh(this);
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(final int slot) {
        if (host != null) {
            host.refresh(this);
        }
        return super.getStackInSlot(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        if (host != null) {
            host.refresh(this);
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (host != null) {
            host.refresh(this);
        }
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        return !TextilesUtils.isItemWithInventory(stack);
    }

    @Override
    protected void onContentsChanged(final int slot) {
        if (host != null) {
            host.onChange(this);
        }
    }

    @Override
    public void setSize(final int size) { // should only ever be called by deserialization
        if (host != null && size != stacks.size()) {
            host.checkSizeChange(this, size);
        }
        stacks.clear();
    }

    public void serializeToStack(final ItemStack stack) {
        NBTTagCompound stackData = stack.getTagCompound();
        final NBTTagCompound tileData;
        if (stackData == null) {
            stackData = new NBTTagCompound();
            tileData = new NBTTagCompound();
            tileData.setString("id", tileEntityId);
            stackData.setTag("BlockEntityTag", tileData);
            stack.setTagCompound(stackData);
        } else if (stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) {
            tileData = stackData.getCompoundTag("BlockEntityTag");
        } else {
            tileData = new NBTTagCompound();
            tileData.setString("id", tileEntityId);
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
        stacks.clear();
        onLoad();
        return false;
    }

    public interface Host {

        void onChange(BlockItemInventory inventory);

        default void refresh(final BlockItemInventory inventory) {}

        default void checkSizeChange(final BlockItemInventory inventory, final int oldSize) {}

        @Desugar
        record Stack(ItemStack stack, boolean doRefresh) implements Host {

            @Override
            public void onChange(final BlockItemInventory inventory) {
                inventory.serializeToStack(stack);
            }

            @Override
            public void refresh(final BlockItemInventory inventory) {
                if (doRefresh) {
                    inventory.deserializeFromStack(stack);
                }
            }

        }

        @Desugar
        record Tile(TileEntity te) implements Host {

            @Override
            public void onChange(final BlockItemInventory inventory) {
                te.markDirty();
            }

            @Override
            public void checkSizeChange(final BlockItemInventory inventory, final int oldSize) {
                final int newSize = inventory.getSlots();
                for (int i = oldSize; i < newSize; i++) {
                    if (!inventory.getStackInSlot(i).isEmpty()) {
                        final World world = te.getWorld();
                        //noinspection ConstantValue
                        if (world == null) {
                            Textiles.LOGGER.warn(
                                    "Inventory at {} has lost contents due to lower configured size!",
                                    te.getPos());
                        } else {
                            Textiles.LOGGER.warn(
                                    "Inventory in world {} at {} has lost contents due to lower configured size!",
                                    world.provider.getDimension(), te.getPos());
                        }
                        return;
                    }
                }
            }

        }

    }

}
