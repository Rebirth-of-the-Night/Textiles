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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import surreal.textiles.ModConfig;
import surreal.textiles.Textiles;
import surreal.textiles.items.ItemBlockSack;
import surreal.textiles.tiles.TileSack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Textiles.MODID, value = Side.CLIENT)
public enum SackInventoryRenderHandler {

    ;

    private static final List<ItemStack> itemBuffer = new ArrayList<>(9);
    private static int backgroundBuffer;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(final ItemTooltipEvent event) {
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemBlockSack)) return;
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
        final int rowSize = ModConfig.sack.slotColumnCount;
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
        final ItemStack sackStack = event.getStack();
        if (!(sackStack.getItem() instanceof ItemBlockSack)) return;
        final IItemHandler inventory = sackStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
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
        final int rowSize = ModConfig.sack.slotColumnCount, rowCount = (inventoryStacks.size() + rowSize - 1) / rowSize;
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

    @Nullable
    private static InteractabilityData cachedInteractability = null;

    @SubscribeEvent
    public static void onDrawScreen(final GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!ModConfig.sack.inventoryInteraction || !(event.getGui() instanceof GuiContainer gui)) return;
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
            final List<SlotInteractability> slotData = new ArrayList<>();
            slotIter:
            for (int i = 0; i < slots.size(); i++) {
                final Slot slot = slots.get(i);
                if (!slot.canTakeStack(player)) continue;
                final ItemStack stack = slot.getStack();
                if (!(stack.getItem() instanceof ItemBlockSack) || !slot.isItemValid(stack)) continue;
                final TileSack.SackInventory sackInventory = new TileSack.SackInventory();
                sackInventory.deserializeFromStack(stack);

                final int originalCount = heldStack.getCount();
                if (ItemHandlerHelper.insertItemStacked(sackInventory, heldStack, true).getCount() < originalCount) {
                    final int slotCount = sackInventory.getSlots();
                    for (int j = 0; j < slotCount; j++) {
                        if (ItemHandlerHelper.canItemStacksStack(sackInventory.getStackInSlot(j), heldStack)) {
                            slotData.add(new SlotInteractability(i, Interactability.CAN_INSERT_ALIKE));
                            continue slotIter;
                        }
                    }
                    slotData.add(new SlotInteractability(i, Interactability.CAN_INSERT));
                } else {
                    slotData.add(new SlotInteractability(i, Interactability.CANNOT_INSERT));
                }
            }
            data = new InteractabilityData(heldStack, gui, slotData);
            cachedInteractability = data;
        }
        if (data.slotData.isEmpty()) return;

        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        final FontRenderer fontRenderer = mc.fontRenderer;
        final int baseX = gui.getGuiLeft(), baseY = gui.getGuiTop();
        final Slot hovered = gui.getSlotUnderMouse();
        for (final SlotInteractability slotDatum : data.slotData) {
            final Slot slot = slots.get(slotDatum.slotIndex);
            if (slot == hovered) continue;
            fontRenderer.drawStringWithShadow("+", baseX + slot.xPos + 10, baseY + slot.yPos + 8, slotDatum.state.col);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableDepth();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(final GuiOpenEvent event) {
        if (event.getGui() == null) {
            cachedInteractability = null; // don't hold up gc
        }
    }

    private enum Interactability {

        CAN_INSERT(0xFFFF55), CAN_INSERT_ALIKE(0x55FF55), CANNOT_INSERT(0xFF5555);

        final int col;

        Interactability(final int col) {
            this.col = col;
        }

    }

    @Desugar
    private record SlotInteractability(int slotIndex, Interactability state) {}

    @Desugar
    private record InteractabilityData(ItemStack heldStack, GuiContainer gui, List<SlotInteractability> slotData) {

        boolean matches(final ItemStack heldStack, final GuiContainer gui) {
            return this.heldStack == heldStack && this.gui == gui;
        }

    }

}
