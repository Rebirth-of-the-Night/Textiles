package surreal.textiles;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import surreal.textiles.client.ClientProxy;
import surreal.textiles.client.guis.GuiHandler;
import surreal.textiles.entities.EntityBasket;
import surreal.textiles.items.ItemMaterial;
import surreal.textiles.tiles.TileBasket;
import surreal.textiles.tiles.TileRawFibers;
import surreal.textiles.tiles.TileSpindle;

import javax.annotation.Nonnull;
import java.util.List;

@Mod(modid = Textiles.MODID, name = "Textiles", version = Tags.VERSION)
public class Textiles {
    
    public static final String MODID = "textiles";

    @Mod.Instance
    public static Textiles INSTANCE;

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
        MinecraftForge.EVENT_BUS.register(new ClientProxy());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        GameRegistry.registerTileEntity(TileSpindle.class, new ResourceLocation(MODID, "spindle"));
        GameRegistry.registerTileEntity(TileRawFibers.class, new ResourceLocation(MODID, "raw_fibers"));
        GameRegistry.registerTileEntity(TileBasket.class, new ResourceLocation(MODID, "basket"));

        FluidRegistry.registerFluid(RegistryManager.FLAXSEED_OIL);
        FluidRegistry.addBucketForFluid(RegistryManager.FLAXSEED_OIL);

        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "basket"), EntityBasket.class, "Basket", 1, INSTANCE, 30, 3, true);
        ClientProxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
    }

    // Gameplay Events
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);

        ItemStack stack = event.getHarvester() != null ? event.getHarvester().getHeldItemMainhand() : ItemStack.EMPTY;

        List<ItemStack> drops = event.getDrops();

        if (!event.isSilkTouching()) {
            if (state.getBlock() == Blocks.TALLGRASS && world.rand.nextFloat() < ModConfig.drops.plantFibersDrop) {
                drops.add(RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.RAW_PLANT_FIBERS));
            }
            else if (ModConfig.drops.replaceCobwebDrop && state.getBlock() == Blocks.WEB && stack.getItem() instanceof ItemSword) {
                drops.clear();
                drops.add(RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.SILK_WISPS));
            }
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
