package surreal.textiles.client.guis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import surreal.textiles.Textiles;
import surreal.textiles.tiles.TileBasket;
import surreal.textiles.tiles.containers.ContainerBasket;

@SideOnly(Side.CLIENT)
public class GuiBasket extends GuiContainer {

    protected static final ResourceLocation BASKET, STURDY_BASKET;

    private final int type;

    public GuiBasket(TileBasket basket) {
        super(new ContainerBasket(basket, Minecraft.getMinecraft().player));
        this.type = basket.getType();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(this.type == 0 ? BASKET : STURDY_BASKET);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, 256, 256);
    }

    static {
        BASKET = new ResourceLocation(Textiles.MODID, "textures/gui/basket.png");
        STURDY_BASKET = new ResourceLocation(Textiles.MODID, "textures/gui/sturdy_basket.png");
    }
}
