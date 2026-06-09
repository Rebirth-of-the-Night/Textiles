package surreal.textiles.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import surreal.textiles.ModConfig;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockBasket;
import surreal.textiles.util.BlockItemInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileBasket extends TileEntity {

    @Nullable
    public static BlockItemInventory wrapStackInventory(final ItemStack stack, final boolean doRefresh) {
        return switch (BlockBasket.Type.fromMeta(stack.getMetadata())) {
            case DEFAULT -> !ModConfig.basket.keepInventory ? null : new BlockItemInventory(
                    BlockBasket.Type.DEFAULT.inventorySize, Default.TILE_ENTITY_ID,
                    stack, doRefresh);
            case STURDY -> !ModConfig.basket.keepInventorySturdy ? null : new BlockItemInventory(
                    BlockBasket.Type.STURDY.inventorySize, Sturdy.TILE_ENTITY_ID,
                    stack, doRefresh);
        };
    }

    private final BlockBasket.Type type;
    private final String tileEntityId;
    private final ItemStackHandler inventory;

    private TileBasket(final BlockBasket.Type type, final String tileEntityId) {
        this.type = type;
        this.tileEntityId = tileEntityId;
        this.inventory = new BlockItemInventory(type.inventorySize, tileEntityId, this);
    }

    public BlockBasket.Type getType() {
        return type;
    }

    public IItemHandlerModifiable getInventory() {
        return inventory;
    }

    public NBTTagCompound writeStateToNBT(final NBTTagCompound tag) {
        tag.setTag("inventory", inventory.serializeNBT());
        return tag;
    }

    public void readStateFromNBT(final NBTTagCompound tag) {
        inventory.deserializeNBT(tag.getCompoundTag("inventory"));
    }

    public NBTTagCompound writeToExternalNBT(final NBTTagCompound tag) {
        writeStateToNBT(tag);
        tag.setString("id", tileEntityId);
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

    public static TileBasket create(final BlockBasket.Type type) {
        return switch (type) {
            case DEFAULT -> new Default();
            case STURDY -> new Sturdy();
        };
    }

    public static class Default extends TileBasket {

        public static final String TILE_ENTITY_ID = Textiles.MODID + ":basket";

        public Default() {
            super(BlockBasket.Type.DEFAULT, TILE_ENTITY_ID);
        }

    }

    public static class Sturdy extends TileBasket {

        public static final String TILE_ENTITY_ID = Textiles.MODID + ":basket_sturdy";

        public Sturdy() {
            super(BlockBasket.Type.STURDY, TILE_ENTITY_ID);
        }

    }

}
