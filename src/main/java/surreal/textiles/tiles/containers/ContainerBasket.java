package surreal.textiles.tiles.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import surreal.textiles.tiles.TileBasket;

import javax.annotation.Nonnull;

public class ContainerBasket extends Container {

    private final TileBasket basket;
    private final IItemHandler handler;

    public ContainerBasket(TileBasket basket, EntityPlayer player) {
        this.basket = basket;
        this.handler = basket.getInventory();

        IInventory playerInventory = player.inventory;

        // 0 = 52, 26
        // 1 = 34, 26
        int slotsInRow = basket.getType() == 0 ? 4 : 6;
        int rows = handler.getSlots() / slotsInRow;

        int start = slotsInRow == 4 ? 52 : 34;
        int x = start;
        int y = 26;

        for (int row = 0; row < rows; row++) {
            for (int i = 0; i < slotsInRow; i++) {
                addSlotToContainer(new SlotItemHandler(handler, slotsInRow * row + i, x, y));
                x += 18;
            }

            x = start;
            y += 18;
        }

        y = 73;

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, y));
            }

            y += 18;
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 131));
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < handler.getSlots()) {
                if (!this.mergeItemStack(itemstack1, this.handler.getSlots(), this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            }
            else if (!this.mergeItemStack(itemstack1, 0, handler.getSlots(), false))
                return ItemStack.EMPTY;

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else slot.onSlotChanged();
        }

        return itemstack;
    }

    public TileBasket getBasket() {
        return basket;
    }
}
