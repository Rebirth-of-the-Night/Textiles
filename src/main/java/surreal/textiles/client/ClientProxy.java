package surreal.textiles.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import surreal.textiles.CommonProxy;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockSack;
import surreal.textiles.client.models.BlockSpindleModel;
import surreal.textiles.client.renderer.RenderEntityBasket;
import surreal.textiles.client.renderer.RenderEntityFallingSack;
import surreal.textiles.entities.EntityBasket;
import surreal.textiles.entities.EntityFallingSack;
import surreal.textiles.items.ItemBlockSack;
import surreal.textiles.tiles.TileSack;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ModelLoaderRegistry.registerLoader(new BlockSpindleModel.Loader());
        RenderingRegistry.registerEntityRenderingHandler(EntityBasket.class, manager -> new RenderEntityBasket(manager, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingSack.class, RenderEntityFallingSack::new);
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RegistryManager.INSTANCE.registerModels(event);
    }

    @SubscribeEvent
    public void registerTextures(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        map.registerSprite(new ResourceLocation(Textiles.MODID, "fluids/flaxseed_oil"));
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (tintIndex == -1) return 0xFFFFFF;
            final int fallingBlockCol = RenderEntityFallingSack.getOverrideBlockCol();
            if (fallingBlockCol >= 0) return fallingBlockCol;
            if (world == null || pos == null || !state.getValue(BlockSack.DYED)
                    || !(world.getTileEntity(pos) instanceof TileSack sack)) return 0xFFFFFF;
            final int col = sack.getDyeColor();
            return col >= 0 ? col : 0xFFFFFF;
        }, RegistryManager.SACK);
        mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == -1) return 0xFFFFFF;
            final int col = ItemBlockSack.getDyeColorForStack(stack);
            return col >= 0 ? col : 0xFFFFFF;
        }, RegistryManager.SACK);
    }

    @Nullable
    @Override
    public RayTraceResult getKnownRayTrace(final EntityPlayer player) {
        final Minecraft mc = Minecraft.getMinecraft();
        return player == mc.player ? mc.objectMouseOver : null;
    }

}
