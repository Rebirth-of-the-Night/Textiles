package surreal.textiles.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReadOnlyItemBlockInventory implements IItemHandler, ICapabilityProvider {

    private final ItemStack stack;
    private final int size;

    public ReadOnlyItemBlockInventory(final ItemStack stack, final int size) {
        this.stack = stack;
        this.size = size;
    }

    public ReadOnlyItemBlockInventory(final ItemStack stack) {
        this(stack, -1);
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this) : null;
    }

    @Nullable
    private NBTTagCompound getBlockData() {
        final NBTTagCompound stackData = stack.getTagCompound();
        return stackData != null && stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)
                ? stackData.getCompoundTag("BlockEntityTag") : null;
    }

    @Override
    public int getSlots() {
        if (size >= 0) return size;
        final NBTTagCompound blockData = getBlockData();
        if (blockData == null) return 0;
        return blockData.getCompoundTag("inventory").getInteger("Size");
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(final int slot) {
        if (slot < 0 || (size >= 0 && slot >= size)) return ItemStack.EMPTY;
        final NBTTagCompound blockData = getBlockData();
        if (blockData == null) return ItemStack.EMPTY;
        final NBTTagCompound invData = blockData.getCompoundTag("inventory");
        if (size < 0 && invData.hasKey("Size", Constants.NBT.TAG_INT) && slot >= invData.getInteger("Size")) {
            return ItemStack.EMPTY;
        }
        final NBTTagList slots = invData.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < slots.tagCount(); i++) {
            final NBTTagCompound slotData = slots.getCompoundTagAt(i);
            if (slotData.getInteger("Slot") == slot) {
                return new ItemStack(slotData);
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(final int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
        return false;
    }

}
