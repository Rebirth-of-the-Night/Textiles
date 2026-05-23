package surreal.textiles.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.input.Mouse;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.items.ItemBlockSack;
import surreal.textiles.network.C2SSackInteraction;
import surreal.textiles.tiles.TileSack;
import surreal.textiles.util.TextilesUtils;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Textiles.MODID)
public enum SackInteractionHandler {

    ;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!ModConfig.sack.inventoryInteraction) return;
        final GuiScreen gui = event.getGui();
        if (!(gui instanceof GuiContainer containerGui)) return;
        final Slot slot = containerGui.getSlotUnderMouse();
        if (slot == null || !isInteractionClick()) return;
        final Minecraft mc = Minecraft.getMinecraft();
        if (gui instanceof GuiContainerCreative creativeGui) {
            final int slotIndex;
            if (creativeGui.getSelectedTabIndex() == CreativeTabs.INVENTORY.getIndex()) {
                slotIndex = slot.slotNumber;
            } else {
                slotIndex = slot.slotNumber - containerGui.inventorySlots.inventorySlots.size() + 45;
                if (slotIndex < 36) return;
            }
            switch (handleSlotClick(mc.player, slot, false)) {
                case SUCCESS:
                    mc.playerController.sendSlotPacket(slot.getStack(), slotIndex);
                    // falls through
                case FAIL:
                    event.setCanceled(true);
                    containerGui.ignoreMouseUp = true;
                    break;
            }
        } else {
            switch (handleSlotClick(mc.player, slot, true)) {
                case SUCCESS:
                    Textiles.NETWORK.sendToServer(
                            new C2SSackInteraction(containerGui.inventorySlots.windowId, slot.slotNumber));
                    // falls through
                case FAIL:
                    event.setCanceled(true);
                    containerGui.ignoreMouseUp = true;
                    break;
            }
        }
    }

    private static boolean isInteractionClick() {
        return Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && !GuiScreen.isShiftKeyDown();
    }

    public static EnumActionResult handleSlotClick(final EntityPlayer player, final Slot slot, final boolean simulate) {
        // TODO is there a better way to check for ghost slots?
        if (!slot.canTakeStack(player)) return EnumActionResult.PASS;

        final InventoryPlayer playerInventory = player.inventory;
        final ItemStack slotStack = slot.getStack();
        if (slotStack.getItem() instanceof ItemBlockSack) {
            if (!slot.isItemValid(slotStack)) return EnumActionResult.PASS;
            final ItemStack heldStack = playerInventory.getItemStack();
            if (heldStack.isEmpty()) {
                final ItemStack resultStack = tryExtract(player, slotStack, null, simulate);
                if (resultStack.isEmpty()) return EnumActionResult.FAIL;
                if (!simulate) {
                    playerInventory.setItemStack(resultStack);
                    slot.putStack(slotStack);
                    slot.onSlotChanged();
                }
                return EnumActionResult.SUCCESS;
            } else {
                final ItemStack remStack = tryInsert(player, slotStack, heldStack, simulate);
                if (remStack == null) return EnumActionResult.FAIL;
                if (!simulate) {
                    playerInventory.setItemStack(remStack);
                    slot.putStack(slotStack);
                    slot.onSlotChanged();
                }
                return EnumActionResult.SUCCESS;
            }
        }

        final ItemStack heldStack = playerInventory.getItemStack();
        if (heldStack.getItem() instanceof ItemBlockSack) {
            if (slotStack.isEmpty()) {
                final ItemStack resultStack = tryExtract(player, heldStack, slot, simulate);
                if (resultStack.isEmpty()) return EnumActionResult.FAIL;
                if (!simulate) {
                    slot.putStack(resultStack);
                    slot.onSlotChanged();
                }
                return EnumActionResult.SUCCESS;
            } else {
                final ItemStack remStack = tryInsert(player, heldStack, slotStack, simulate);
                if (remStack == null) return EnumActionResult.FAIL;
                if (!simulate) {
                    final ItemStack takenStack = ItemHandlerHelper.copyStackWithSize(
                            slotStack, slotStack.getCount() - remStack.getCount());
                    final ItemStack newSlotStack = slot.decrStackSize(takenStack.getCount());
                    if (newSlotStack.isEmpty()) {
                        slot.putStack(ItemStack.EMPTY);
                    }
                    slot.onTake(player, takenStack);
                    // Container#slotClick calls slot.onSlotChanged() here, but it's probably not necessary
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    private static ItemStack tryExtract(final EntityPlayer player, final ItemStack sackStack,
                                        @Nullable final Slot destSlot, final boolean simulate) {
        final TileSack.SackInventory sackInventory = new TileSack.SackInventory(sackStack);
        if (!sackInventory.deserializeFromStack(sackStack)) return ItemStack.EMPTY;

        // may diverge from the random selection on the server, but it's only simulated on the client anyways
        final int slotIndex = TextilesUtils.getRandomNonEmptySlot(sackInventory, player.world.rand);
        if (slotIndex < 0 || (destSlot != null && !destSlot.isItemValid(sackInventory.getStackInSlot(slotIndex)))) {
            return ItemStack.EMPTY;
        }

        final ItemStack resultStack = sackInventory.extractItem(slotIndex, Integer.MAX_VALUE, simulate);
        if (resultStack.isEmpty()) return ItemStack.EMPTY;
        player.playSound(RegistryManager.SACK_EXTRACT, 0.75F, 1F);
        return resultStack;
    }

    @Nullable
    private static ItemStack tryInsert(final EntityPlayer player, final ItemStack sackStack,
                                       final ItemStack insertStack, final boolean simulate) {
        if (TextilesUtils.isItemWithInventory(insertStack)) return null;

        final TileSack.SackInventory sackInventory = new TileSack.SackInventory(sackStack);
        sackInventory.deserializeFromStack(sackStack);

        if (simulate) {
            final ItemStack remStack = ItemHandlerHelper.insertItemStacked(sackInventory, insertStack, true);
            if (remStack.getCount() >= insertStack.getCount()) return null;
            player.playSound(RegistryManager.SACK_INSERT, 0.75F, 1F);
            return remStack;
        }

        final ItemStack remStack = ItemHandlerHelper.insertItemStacked(sackInventory, insertStack.copy(), false);
        if (remStack.getCount() >= insertStack.getCount()) return null;
        player.playSound(RegistryManager.SACK_INSERT, 0.75F, 1F);
        sackInventory.serializeToStack(sackStack);
        return remStack;
    }

}
