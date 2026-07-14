package surreal.textiles.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import surreal.textiles.Textiles;
import surreal.textiles.entities.EntityWoolCushion;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class RenderEntityWoolCushion extends Render<EntityWoolCushion> {

    private static final ResourceLocation CUSHION_MODEL_BASE = new ResourceLocation(Textiles.MODID, "small_cushion");
    public static final Map<EnumDyeColor, ModelResourceLocation> CUSHION_MODELS;

    static {
        final Map<EnumDyeColor, ModelResourceLocation> models = new EnumMap<>(EnumDyeColor.class);
        for (final EnumDyeColor variant : EnumDyeColor.values()) {
            models.put(variant, new ModelResourceLocation(CUSHION_MODEL_BASE, "color=" + variant.getName()));
        }
        CUSHION_MODELS = Collections.unmodifiableMap(models);
    }

    public RenderEntityWoolCushion(final RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(final EntityWoolCushion entity, final double x, final double y, final double z,
                         final float entityYaw, final float partialTicks) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        if (renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(getTeamColor(entity));
        }

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        GlStateManager.translate(
                (float) (x - pos.getX() - 0.5D),
                (float) (y - pos.getY()),
                (float) (z - pos.getZ() - 0.5D));
        final BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        final IBakedModel model = brd.getBlockModelShapes().getModelManager()
                .getModel(CUSHION_MODELS.get(entity.getVariant()));
        brd.getBlockModelRenderer().renderModel(
                entity.world, model, Blocks.AIR.getDefaultState(), pos, buf, false, MathHelper.getPositionRandom(pos));
        tess.draw();

        if (renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(final EntityWoolCushion entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
