package surreal.textiles;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import surreal.textiles.client.guis.GuiHandler;
import surreal.textiles.entities.EntityBasket;
import surreal.textiles.entities.EntityFallingSack;
import surreal.textiles.event.OverencumbranceHandler;
import surreal.textiles.items.ItemMaterial;
import surreal.textiles.network.C2SSackInteraction;
import surreal.textiles.tiles.TileBasket;
import surreal.textiles.tiles.TileRawFibers;
import surreal.textiles.tiles.TileSack;
import surreal.textiles.tiles.TileSpindle;

import javax.annotation.Nonnull;

@Mod(modid = Textiles.MODID, name = "Textiles", version = Tags.VERSION)
public class Textiles {

    public static final String MODID = "textiles";

    @Mod.Instance
    public static Textiles INSTANCE;

    @SidedProxy(
            modId = Textiles.MODID,
            serverSide = "surreal.textiles.CommonProxy",
            clientSide = "surreal.textiles.client.ClientProxy"
    )
    public static CommonProxy proxy;

    public static Logger LOGGER;
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    public static CreativeTabs TAB = new CreativeTabs(MODID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.FLAX_STALKS);
        }
    };

    static {
        FluidRegistry.enableUniversalBucket();
    }

    @Mod.EventHandler
    public void construction(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(proxy);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();

        NETWORK.registerMessage(new C2SSackInteraction.Handler(), C2SSackInteraction.class, 0, Side.SERVER);

        GameRegistry.registerTileEntity(TileSpindle.class, new ResourceLocation(MODID, "spindle"));
        GameRegistry.registerTileEntity(TileRawFibers.class, new ResourceLocation(MODID, "raw_fibers"));
        GameRegistry.registerTileEntity(TileBasket.class, new ResourceLocation(MODID, "basket"));
        GameRegistry.registerTileEntity(TileSack.class, new ResourceLocation(MODID, "sack"));

        FluidRegistry.registerFluid(RegistryManager.FLAXSEED_OIL);
        FluidRegistry.addBucketForFluid(RegistryManager.FLAXSEED_OIL);

        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "basket"), EntityBasket.class, "Basket", 1, INSTANCE, 30, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "falling_sack"), EntityFallingSack.class, "Falling Sack", 2, INSTANCE, 160, 20, true);

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
        OverencumbranceHandler.loadConfig();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }

    // Registry Events
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegistryManager.INSTANCE.registerBlocks(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegistryManager.INSTANCE.registerItems(event);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        RegistryManager.INSTANCE.registerRecipes(event);
    }
}
