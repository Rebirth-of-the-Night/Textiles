package surreal.textiles.tiles.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import surreal.textiles.ModConfig;
import surreal.textiles.tiles.TileSack;

public class ContainerSack extends Container {

    private final TileSack sack;

    public ContainerSack(final TileSack sack, final EntityPlayer player) {
        this.sack = sack;
        sack.onOpened(player);

        final IItemHandlerModifiable sackInventory = sack.getInventory();
        final int rowCount = ModConfig.sack.slotRowCount, colCount = ModConfig.sack.slotColumnCount;
        final int baseX = 8 + (18 * (9 - colCount)) / 2, baseY = 18 + (18 * (3 - rowCount)) / 2;
        for (int rowNdx = 0; rowNdx < rowCount; rowNdx++) {
            for (int colNdx = 0; colNdx < colCount; colNdx++) {
                addSlotToContainer(new SlotItemHandler(
                        sackInventory, rowNdx * colCount + colNdx, baseX + colNdx * 18, baseY + rowNdx * 18));
            }
        }

        final InventoryPlayer playerInventory = player.inventory;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return player.getDistanceSqToCenter(sack.getPos()) <= 64D;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;

        final ItemStack stack = slot.getStack();
        final ItemStack originalStack = stack.copy();
        if (index < 9) {
            if (!mergeItemStack(stack, 9, 45, true)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(stack, 0, 9, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (originalStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return originalStack;
    }

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        sack.onClosed(player);
    }

    public boolean isSameSack(final TileSack otherSack) {
        return sack == otherSack;
    }

}
