package surreal.textiles.client.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import surreal.textiles.entities.EntityFallingSack;

import javax.annotation.Nullable;

public class RenderEntityFallingSack extends Render<EntityFallingSack> {

    private static int overrideBlockCol = -1;

    public static int getOverrideBlockCol() {
        return overrideBlockCol;
    }

    private final RenderFallingBlock delegate;

    public RenderEntityFallingSack(final RenderManager renderManager) {
        super(renderManager);
        delegate = new RenderFallingBlock(renderManager);
    }

    @Override
    public void doRender(final EntityFallingSack entity, final double x, final double y, final double z,
                         final float entityYaw, final float partialTicks) {
        overrideBlockCol = entity.getDyeColor();
        try {
            delegate.doRender(entity, x, y, z, entityYaw, partialTicks);
        } finally {
            overrideBlockCol = -1;
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(final EntityFallingSack entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
