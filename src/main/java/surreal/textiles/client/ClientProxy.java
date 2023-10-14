package surreal.textiles.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.client.renderer.RenderEntityBasket;
import surreal.textiles.entities.EntityBasket;

@SideOnly(Side.CLIENT)
public class ClientProxy {

    public static void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityBasket.class, manager -> new RenderEntityBasket(manager, Minecraft.getMinecraft().getRenderItem()));
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
}
