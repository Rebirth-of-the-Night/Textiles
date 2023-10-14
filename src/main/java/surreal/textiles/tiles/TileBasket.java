package surreal.textiles.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileBasket extends TileEntity {

    private final ItemStackHandler inventory;
    private final int type;

    public TileBasket() {
        this(0);
    }

    public TileBasket(int type) {
        this.type = type;
        inventory = new ItemStackHandler(type == 0 ? 8 : 12);
    }

    public int getType() {
        return type;
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public boolean shouldDrop() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) return true;
        }

        return false;
    }

    public NBTTagCompound writeInventoryToNBT(NBTTagCompound tag) {
        tag.setTag("inventory", inventory.serializeNBT());
        return tag;
    }

    public void readInventoryFromNBT(NBTTagCompound tag) {
        inventory.deserializeNBT(tag.getCompoundTag("BlockEntityTag").getCompoundTag("inventory"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("inventory", inventory.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
    }

    // Capability
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        else return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) inventory;
        else return super.getCapability(capability, facing);
    }
}
