package surreal.textiles.util;

import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.Random;

public enum TextilesUtils {

    ;

    public static boolean isItemWithInventory(final ItemStack stack) {
        return stack.getItem() instanceof ItemShulkerBox
                || stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Nullable
    public static IItemHandler getItemInventoryForRead(final World world, final ItemStack stack) {
        final IItemHandler inventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (inventory != null) return inventory;
        if (!(stack.getItem() instanceof ItemShulkerBox)) return null;
        final NBTTagCompound stackData = stack.getTagCompound();
        if (stackData == null || !stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) return null;
        final TileEntityShulkerBox shulkerBox = new TileEntityShulkerBox();
        shulkerBox.setWorld(world);
        shulkerBox.readFromNBT(stackData.getCompoundTag("BlockEntityTag"));
        return new InvWrapper(shulkerBox);
    }

    public static int getRandomNonEmptySlot(final IItemHandler inventory, final Random random) {
        int currentSlot = -1;
        int currentOdds = 1;
        final int slotCount = inventory.getSlots();
        for (int i = 0; i < slotCount; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) continue;
            if (currentOdds == 1 || random.nextInt(currentOdds) == 0) {
                currentSlot = i;
            }
            currentOdds++;
        }
        return currentSlot;
    }

}
