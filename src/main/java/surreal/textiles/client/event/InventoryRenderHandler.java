package surreal.textiles.client.event;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import surreal.textiles.ModConfig;
import surreal.textiles.items.PortableInventoryItem;
import surreal.textiles.util.BlockItemInventory;
import surreal.textiles.util.TextilesUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public enum InventoryRenderHandler {

    ;

    // RENDER CONTENTS

    private static final List<ItemStack> itemBuffer = new ArrayList<>(9);
    private static int backgroundBuffer;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(final ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof PortableInventoryItem)) return;
        final IItemHandler inventory = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (inventory == null) return;

        int itemCount = 0;
        final int slotCount = inventory.getSlots();
        for (int i = 0; i < slotCount; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                itemCount++;
            }
        }
        if (itemCount <= 0) return;

        final List<String> tooltip = event.getToolTip();
        final int rowSize = TextilesUtils.getLeastSquareBound(itemCount);
        if (itemCount <= rowSize) {
            final String line = "§l" + StringUtils.repeat("    ", itemCount);
            tooltip.add(line);
            tooltip.add(line);
        } else {
            final String line = "§l" + StringUtils.repeat("    ", rowSize);
            while (itemCount > 0) {
                tooltip.add(line);
                tooltip.add(line);
                itemCount -= rowSize;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltipColor(final RenderTooltipEvent.Color event) {
        backgroundBuffer = event.getBackground();
    }

    @SubscribeEvent
    public static void onRenderTooltip(final RenderTooltipEvent.PostBackground event) {
        final ItemStack invStack = event.getStack();
        if (!(invStack.getItem() instanceof PortableInventoryItem)) return;
        final IItemHandler inventory = invStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (inventory == null) return;

        final int slotCount = inventory.getSlots();
        final List<ItemStack> inventoryStacks = itemBuffer;
        for (int i = 0; i < slotCount; i++) {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inventoryStacks.add(stack);
            }
        }
        if (inventoryStacks.isEmpty()) return;

        final Minecraft mc = Minecraft.getMinecraft();
        final RenderItem itemRenderer = mc.getRenderItem();
        final FontRenderer fontRenderer = event.getFontRenderer();
        final int rowSize = TextilesUtils.getLeastSquareBound(inventoryStacks.size());
        final int rowCount = (inventoryStacks.size() + rowSize - 1) / rowSize;
        final int tooltipX = event.getX(), maxX = tooltipX + 20 * rowSize;
        final int bg = backgroundBuffer;
        final int slotBg = (bg & 0xFF000000)
                | Math.min((int) Math.floor(((bg >>> 16) & 0xFF) * 0.75F + 32), 255) << 16
                | Math.min((int) Math.floor(((bg >>> 8) & 0xFF) * 0.75F + 32), 255) << 8
                | Math.min((int) Math.floor((bg & 0xFF) * 0.75F + 32), 255);
        try {
            RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.zLevel = 300;

            int x = tooltipX, y = event.getY() + event.getHeight() - 20 * rowCount + 1;
            for (final ItemStack stack : inventoryStacks) {
                if (stack.isEmpty()) continue;

                // this might look wrong if the tooltip wraps, but that's pretty unlikely
                final int itemX = x + 2, itemY = y + 2;
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
                GuiUtils.drawGradientRect(200, x + 1, y + 1, x + 19, y + 19, slotBg, slotBg);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                itemRenderer.renderItemAndEffectIntoGUI(stack, itemX, itemY);
                itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, itemX, itemY, null);

                x += 20;
                if (x >= maxX) {
                    x = tooltipX;
                    y += 20;
                }
            }

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
        } finally {
            inventoryStacks.clear();
            itemRenderer.zLevel = 0;
        }
    }

    // RENDER INTERACTION INDICATORS

    @Nullable
    private static InteractabilityData cachedInteractability = null;

    @SubscribeEvent
    public static void onDrawScreen(final GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(ModConfig.basket.inventoryInteraction || ModConfig.sack.inventoryInteraction)
                || !(event.getGui() instanceof GuiContainer gui)) return;
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final ItemStack heldStack = player.inventory.getItemStack();
        if (heldStack.isEmpty()) return;

        final List<Slot> slots = gui.inventorySlots.inventorySlots;
        final InteractabilityData data;
        final InteractabilityData cachedData = cachedInteractability;
        if (cachedData != null && cachedData.matches(heldStack, gui)) {
            data = cachedData;
        } else {
            final SlotInteractability[] slotData = new SlotInteractability[slots.size()];
            for (int i = 0; i < slots.size(); i++) {
                slotData[i] = computeInteractability(slots.get(i), player, heldStack);
            }
            data = new InteractabilityData(heldStack, gui, slotData);
            cachedInteractability = data;
        }
        final SlotInteractability[] slotData = data.slotData;
        if (slotData.length == 0) return;

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        final FontRenderer fontRenderer = mc.fontRenderer;
        final int baseX = gui.getGuiLeft(), baseY = gui.getGuiTop();
        final Slot hovered = gui.getSlotUnderMouse();
        for (int i = 0; i < slotData.length; i++) {
            final Slot slot = slots.get(i);
            if (slot == hovered) continue;
            final ItemStack slotStack = slot.getStack();
            SlotInteractability entry = slotData[i];
            if (entry == null || !entry.matches(slotStack)) {
                if (slotStack.isEmpty()) continue;
                entry = computeInteractability(slot, player, heldStack);
                slotData[i] = entry;
            }
            if (entry != null) {
                fontRenderer.drawStringWithShadow("+", baseX + slot.xPos + 10, baseY + slot.yPos + 8, entry.state.col);
            }
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableDepth();
    }

    @Nullable
    private static SlotInteractability computeInteractability(final Slot slot, final EntityPlayer player,
                                                              final ItemStack heldStack) {
        if (!slot.canTakeStack(player)) return null;
        final ItemStack stack = slot.getStack();
        if (!(stack.getItem() instanceof PortableInventoryItem invItem) || !slot.isItemValid(stack)) return null;
        final BlockItemInventory inventory = invItem.getPortableInventory(stack);
        if (inventory == null) return null;
        inventory.deserializeFromStack(stack);

        final int originalCount = heldStack.getCount();
        if (ItemHandlerHelper.insertItemStacked(inventory, heldStack, true).getCount() < originalCount) {
            final int slotCount = inventory.getSlots();
            for (int j = 0; j < slotCount; j++) {
                if (ItemHandlerHelper.canItemStacksStack(inventory.getStackInSlot(j), heldStack)) {
                    return new SlotInteractability(stack, Interactability.CAN_INSERT_ALIKE);
                }
            }
            return new SlotInteractability(stack, Interactability.CAN_INSERT);
        } else {
            return new SlotInteractability(stack, Interactability.CANNOT_INSERT);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGuiOpen(final GuiOpenEvent event) { // also fired on gui close with gui = null
        cachedInteractability = null; // don't hold up gc
    }

    private enum Interactability {

        CAN_INSERT(0xFFFF55), CAN_INSERT_ALIKE(0x55FF55), CANNOT_INSERT(0xFF5555);

        final int col;

        Interactability(final int col) {
            this.col = col;
        }

    }

    @Desugar
    private record SlotInteractability(ItemStack stack, Interactability state) {

        boolean matches(final ItemStack stack) {
            return this.stack == stack;
        }

    }

    @Desugar
    private record InteractabilityData(ItemStack heldStack, GuiContainer gui, SlotInteractability[] slotData) {

        boolean matches(final ItemStack heldStack, final GuiContainer gui) {
            return this.heldStack == heldStack && this.gui == gui
                    && slotData.length == gui.inventorySlots.inventorySlots.size();
        }

    }

}
