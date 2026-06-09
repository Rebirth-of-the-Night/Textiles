package surreal.textiles.items;

import net.minecraft.item.ItemStack;
import surreal.textiles.util.BlockItemInventory;

import javax.annotation.Nullable;

public interface PortableInventoryItem {

    @Nullable
    BlockItemInventory getPortableInventory(ItemStack stack);

}
