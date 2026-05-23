package surreal.textiles.client.guis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import surreal.textiles.Textiles;
import surreal.textiles.tiles.TileSack;
import surreal.textiles.tiles.containers.ContainerSack;

import java.util.List;

public class GuiSack extends GuiContainer {

    private static final ResourceLocation BG_TEXTURE = new ResourceLocation(Textiles.MODID, "textures/gui/sack.png");

    public GuiSack(final TileSack sack) {
        super(new ContainerSack(sack, Minecraft.getMinecraft().player));
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        final int baseX = guiLeft, baseY = guiTop;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BG_TEXTURE);
        drawTexturedModalRect(baseX, baseY, 0, 0, xSize, ySize);

        final List<Slot> slots = inventorySlots.inventorySlots;
        final int slotCount = TileSack.getConfiguredSize();
        for (int i = 0; i < slotCount; i++) {
            final Slot slot = slots.get(i);
            drawTexturedModalRect(baseX + slot.xPos - 1, baseY + slot.yPos - 1, 176, 0, 18, 18);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        fontRenderer.drawString(I18n.format("tile.textiles.sack.name"), 8, 6, 0x404040);
        fontRenderer.drawString(Minecraft.getMinecraft().player.inventory.getDisplayName().getUnformattedText(),
                8, ySize - 96 + 3, 0x404040);
    }

}
