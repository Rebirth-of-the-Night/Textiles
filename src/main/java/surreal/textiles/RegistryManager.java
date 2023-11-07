package surreal.textiles;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;
import surreal.textiles.blocks.*;
import surreal.textiles.client.models.ModelRegistry;
import surreal.textiles.items.*;

import java.util.List;
import java.util.Objects;

import static surreal.textiles.Textiles.MODID;
import static surreal.textiles.Textiles.TAB;
import static surreal.textiles.items.ItemMaterial.Type.*;
import static net.minecraft.block.BlockPlanks.EnumType.*;

public class RegistryManager {

    public static final RegistryManager INSTANCE = new RegistryManager();

    private final List<Block> BLOCKS;
    private final List<Item> ITEMS;

    private final List<Block> DOUBLE_CUSHIONS;

    private final List<Item> FABRICS;
    private final List<Item> CUSHIONS;

    // Blocks
    public static BlockFeather FEATHER_BLOCK;
    public static BlockFlax FLAX_CROP;
    public static BlockFibers RAW_FIBERS, DRIED_FIBERS;
    public static BlockBasket BASKET;

    // Items
    public static ItemMaterial MATERIAL;
    public static ItemSeeds FLAX_SEEDS;

    public static Fluid FLAXSEED_OIL;

    public RegistryManager() {
        BLOCKS = new ObjectArrayList<>();
        ITEMS = new ObjectArrayList<>();

        DOUBLE_CUSHIONS = new ObjectArrayList<>();

        FABRICS = new ObjectArrayList<>();
        CUSHIONS = new ObjectArrayList<>();

        // Blocks
        FEATHER_BLOCK = registerBlock("feather_block", new BlockFeather());
        registerItem("feather_block", new ItemBlockBase(FEATHER_BLOCK));

        FLAX_CROP = registerBlock("flax_crop", new BlockFlax());

        RAW_FIBERS = registerBlock("raw_fibers", new BlockRawFibers());
        DRIED_FIBERS = registerBlock("dried_fibers", new BlockRettedFibers());
        registerItem("raw_fibers", new ItemBlockStackable(RAW_FIBERS));
        registerItem("dried_fibers", new ItemBlockStackable(DRIED_FIBERS));

        registerSpindles();
        registerCushions();

        BASKET = registerBlock("basket", new BlockBasket());
        registerItem("basket", new ItemBlockBasket(BASKET));

        // Items
        MATERIAL = registerItem("material", new ItemMaterial());
        FLAX_SEEDS = registerItem("flax_seeds", new ItemFlaxSeeds(FLAX_CROP));

        // Fluids
        ResourceLocation oilLocation = new ResourceLocation(MODID, "fluids/flaxseed_oil");
        FLAXSEED_OIL = new Fluid("flaxseed_oil", oilLocation, oilLocation);
    }

    public ItemBlock getItemBlock(Block block) {
        return (ItemBlock) Item.getItemFromBlock(block);
    }

    public ItemStack getMaterial(ItemMaterial.Type type, int amount) {
        return new ItemStack(MATERIAL, amount, type.ordinal());
    }

    public ItemStack getMaterial(ItemMaterial.Type type) {
        return getMaterial(type, 1);
    }

    public <T extends Block> T registerBlock(String registryName, T block) {
        block.setRegistryName(MODID, registryName).setTranslationKey(MODID + "." + registryName).setCreativeTab(TAB);
        BLOCKS.add(block);
        return block;
    }

    public <T extends Item> T registerItem(String registryName, T item) {
        item.setRegistryName(MODID, registryName).setTranslationKey(MODID + "." + registryName);
        item.setCreativeTab(TAB);

        ITEMS.add(item);

        return item;
    }

    // Registries for blocks that i'm lazy to create instance one by one for
    private void registerSpindles() {
        FABRICS.add(registerItem("spindle", new ItemBlockStackable(registerBlock("spindle", new BlockSpindle()))));

        for (int i = 0; i < 16; i++) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(i);
            String regName = "spindle_" + color.getName();

            FABRICS.add(registerItem(regName, new ItemBlockStackable(registerBlock(regName, new BlockSpindle()))));
        }
    }

    private void registerCushions() {
        BlockSlab cushion = registerBlock("cushion", new BlockCushion());
        BlockSlab cushionDouble = registerBlock("cushion_double", new BlockCushion.BlockCushionDouble(cushion));

        CUSHIONS.add(registerItem("cushion", new ItemCushion(cushionDouble, cushion)));
        DOUBLE_CUSHIONS.add(cushionDouble);

        for (int i = 0; i < 16; i++) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(i);
            String regName = "cushion_" + color.getName();

            BlockSlab c = registerBlock(regName, new BlockCushion());
            BlockSlab cD = registerBlock("cushion_double_" + color.getName(), new BlockCushion.BlockCushionDouble(c));

            CUSHIONS.add(registerItem(regName, new ItemCushion(cD, c)));
            DOUBLE_CUSHIONS.add(cD);
        }
    }

    // Events
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        BLOCKS.forEach(event.getRegistry()::register);
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        ITEMS.forEach(event.getRegistry()::register);
    }

    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();

        ItemStack flaxStalk = getMaterial(FLAX_STALKS);

        ItemStack wickerPatch = getMaterial(WICKER_PATCH);
        wickerPatch.setCount(3);

        ItemStack chainMesh = getMaterial(CHAIN_MESH);
        chainMesh.setCount(5);

        ItemStack rawPlantFibers = getMaterial(RAW_PLANT_FIBERS);
        ItemStack silkThread = getMaterial(SILK_THREAD);
        ItemStack silkWisp = getMaterial(SILK_WISPS);
        ItemStack woodTar = getMaterial(WOOD_STAIN);
        ItemStack woodBleach = getMaterial(WOOD_BLEACH);
        ItemStack flaxSeeds = new ItemStack(FLAX_SEEDS);

        ItemStack flaxseedOilBottle = getMaterial(FLAXSEED_OIL_BOTTLE);
        ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(FLAXSEED_OIL, Fluid.BUCKET_VOLUME));

        Ingredient bucketIng = Ingredient.fromStacks(bucket);
        Ingredient bottle = Ingredient.fromItem(Items.GLASS_BOTTLE);

        Ingredient flaxSeedsIng = Ingredient.fromStacks(flaxSeeds);

        ItemStack rawFibers = new ItemStack(RAW_FIBERS);
        Ingredient rawPlantFibersIng = Ingredient.fromStacks(rawPlantFibers);

        // Flax Seed
        MinecraftForge.addGrassSeed(flaxSeeds, 2);

        // Wicker Patch
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, WICKER_PATCH.getName()), null, wickerPatch, "ABA", "BAB", "ABA", 'A', flaxStalk, 'B', "stickWood");
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, WICKER_PATCH.getName() + "_2"), null, wickerPatch, "ABA", "BAB", "ABA", 'A', "sugarcane", 'B', "stickWood");

        // Twine
        OreDictionary.registerOre("string", getMaterial(TWINE));

        // Chain Mesh
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, CHAIN_MESH.getName()), null,  chainMesh, "ABA", "BAB", "ABA", 'A', "nuggetIron", 'B', "ingotIron");

        // Feather Block
        GameRegistry.addShapedRecipe(FEATHER_BLOCK.getRegistryName(), null, new ItemStack(FEATHER_BLOCK), "AAA", "AAA", "AAA", 'A', "feather");

        // Raw Plant Fibers
        Ingredient deadBush = Ingredient.fromStacks(new ItemStack(Blocks.DEADBUSH));
        Ingredient tallGrass = Ingredient.fromStacks(new ItemStack(Blocks.TALLGRASS, 1, OreDictionary.WILDCARD_VALUE));

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, RAW_PLANT_FIBERS.getName() + "_from_dead_bush"), null, rawPlantFibers, deadBush, deadBush, deadBush, deadBush);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, RAW_PLANT_FIBERS.getName() + "_from_tall_grass"), null, rawPlantFibers, "AA ", " A ", " AA", 'A', tallGrass);

        // Silk Thread
        Ingredient silkWispIng = Ingredient.fromStacks(silkWisp);

        OreDictionary.registerOre("string", silkThread);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, SILK_THREAD.getName()), null, silkThread, "AAA", "ABA", "AAA", 'A', silkWispIng, 'B', "stickWood");

        // Silk Wisps
        silkWisp = silkWisp.copy();
        silkWisp.setCount(9);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, SILK_WISPS.getName()), null, silkWisp, Ingredient.fromStacks(new ItemStack(Blocks.WEB)));

        // Flaxseed Oil Bottle
        ItemStack flaxseedOil3 = flaxseedOilBottle.copy();
        flaxseedOil3.setCount(3);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, FLAXSEED_OIL_BOTTLE.getName()), null, flaxseedOilBottle, flaxSeedsIng, flaxSeedsIng, bottle);
        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, FLAXSEED_OIL.getName() + "_from_bucket"), null, flaxseedOil3, bottle, bottle, bottle, bucketIng);

        // Wood Tar
        Ingredient brownMushroom = Ingredient.fromStacks(new ItemStack(Blocks.BROWN_MUSHROOM));

        ItemStack woodTar3 = woodTar.copy();
        woodTar3.setCount(3);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, WOOD_STAIN.getName()), null, woodTar3, bottle, bottle, bottle, bucketIng, brownMushroom, brownMushroom);

        // Wood Bleach
        Ingredient redMushroom = Ingredient.fromStacks(new ItemStack(Blocks.RED_MUSHROOM));

        ItemStack woodBleach3 = woodBleach.copy();
        woodBleach3.setCount(3);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, WOOD_BLEACH.getName()), null, woodBleach3, bottle, bottle, bottle, bucketIng, redMushroom, redMushroom);

        // Flaxseed Oil Bucket
        Ingredient flaxseedOilBottleIng = Ingredient.fromStacks(flaxseedOilBottle);
        Ingredient emptyBucket = Ingredient.fromItem(Items.BUCKET);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, FLAXSEED_OIL.getName()), null, bucket, flaxseedOilBottleIng, flaxseedOilBottleIng, flaxseedOilBottleIng, emptyBucket);
        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, FLAXSEED_OIL.getName() + "_from_seeds"), null, bucket, flaxSeedsIng, flaxSeedsIng, flaxSeedsIng, flaxSeedsIng, flaxSeedsIng, flaxSeedsIng, emptyBucket);

        // Colored Stuff
        registerColoredRecipes(event);

        // Fiber
        GameRegistry.addShapedRecipe(RAW_FIBERS.getRegistryName(), null, rawFibers, "AA ", "AAA", " AA", 'A', rawPlantFibersIng);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, Objects.requireNonNull(RAW_FIBERS.getRegistryName()).getPath() + "_flax"), null, rawFibers, "AA", "AA", 'A', Ingredient.fromStacks(flaxStalk));

        // MC Stuffs
        Ingredient paleBlossoms = Ingredient.fromStacks(getMaterial(PALE_FLAX_BLOSSOMS));
        Ingredient vibrantBlossoms = Ingredient.fromStacks(getMaterial(VIBRANT_FLAX_BLOSSOMS));
        Ingredient exquisiteBlossoms = Ingredient.fromStacks(getMaterial(EXQUISITE_FLAX_BLOSSOMS));

        Ingredient chainMeshIng = Ingredient.fromStacks(chainMesh);

        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, "lightblue_dye_from_pale_blossoms"), null, getColorItem(EnumDyeColor.LIGHT_BLUE), paleBlossoms, paleBlossoms, paleBlossoms);
        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, "cyan_dye_from_vibrant_blossoms"), null, getColorItem(EnumDyeColor.CYAN), vibrantBlossoms, vibrantBlossoms, vibrantBlossoms);
        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, "purple_from_exquisite_blossoms"), null, getColorItem(EnumDyeColor.PURPLE), exquisiteBlossoms, exquisiteBlossoms, exquisiteBlossoms);

        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "chainmail_helmet"), null, new ItemStack(Items.CHAINMAIL_HELMET), "AAA", "A A", 'A', chainMeshIng);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "chainmail_chestplate"), null, new ItemStack(Items.CHAINMAIL_CHESTPLATE), "A A", "AAA", "AAA", 'A', chainMeshIng);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "chainmail_leggings"), null, new ItemStack(Items.CHAINMAIL_LEGGINGS), "AAA", "A A", "A A", 'A', chainMeshIng);
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "chainmail_boots"), null, new ItemStack(Items.CHAINMAIL_BOOTS), "A A", "A A", 'A', chainMeshIng);

        // Baskets
        Ingredient wickerPatchIng = Ingredient.fromStacks(getMaterial(WICKER_PATCH));
        registry.register(lessStupidOreRecipe(BASKET.getRegistryName(), new ItemStack(BASKET), "ABA", "A A", "AAA", 'A', wickerPatchIng, 'B', "stickWood"));
        registry.register(lessStupidOreRecipe(new ResourceLocation(BASKET.getRegistryName() + "_sturdy"), new ItemStack(BASKET, 1, 1), "ABA", "ACA", "AAA", 'A', wickerPatchIng, 'B', "stickWood", 'C', new ItemStack(Items.BUCKET)));

        // Wood Staining
        addWoodRecipe(0, new ItemStack(Blocks.PLANKS, 1, OAK.getMetadata()), new ItemStack(Blocks.PLANKS, 1, SPRUCE.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.PLANKS, 1, BIRCH.getMetadata()), new ItemStack(Blocks.PLANKS, 1, OAK.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.PLANKS, 1, JUNGLE.getMetadata()), new ItemStack(Blocks.PLANKS, 1, ACACIA.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.PLANKS, 1, SPRUCE.getMetadata()), new ItemStack(Blocks.PLANKS, 1, DARK_OAK.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.PLANKS, 1, BIRCH.getMetadata()), new ItemStack(Blocks.PLANKS, 1, JUNGLE.getMetadata()), true, true);

        addWoodRecipe(0, new ItemStack(Blocks.OAK_STAIRS), new ItemStack(Blocks.SPRUCE_STAIRS));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_STAIRS), new ItemStack(Blocks.OAK_STAIRS));
        addWoodRecipe(0, new ItemStack(Blocks.JUNGLE_STAIRS), new ItemStack(Blocks.ACACIA_STAIRS));
        addWoodRecipe(0, new ItemStack(Blocks.SPRUCE_STAIRS), new ItemStack(Blocks.DARK_OAK_STAIRS));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_STAIRS), new ItemStack(Blocks.DARK_OAK_STAIRS));

        addWoodRecipe(0, new ItemStack(Blocks.OAK_FENCE), new ItemStack(Blocks.SPRUCE_FENCE));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Blocks.OAK_FENCE));
        addWoodRecipe(0, new ItemStack(Blocks.JUNGLE_FENCE), new ItemStack(Blocks.ACACIA_FENCE));
        addWoodRecipe(0, new ItemStack(Blocks.SPRUCE_FENCE), new ItemStack(Blocks.DARK_OAK_FENCE));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Blocks.JUNGLE_FENCE), true, true);

        addWoodRecipe(0, new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.SPRUCE_FENCE_GATE));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.OAK_FENCE_GATE));
        addWoodRecipe(0, new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.ACACIA_FENCE_GATE));
        addWoodRecipe(0, new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_FENCE_GATE));
        addWoodRecipe(0, new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.JUNGLE_FENCE_GATE), true, true);

        addWoodRecipe(0, new ItemStack(Blocks.WOODEN_SLAB, 1, OAK.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, SPRUCE.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.WOODEN_SLAB, 1, BIRCH.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, OAK.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.WOODEN_SLAB, 1, JUNGLE.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, ACACIA.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.WOODEN_SLAB, 1, SPRUCE.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, DARK_OAK.getMetadata()));
        addWoodRecipe(0, new ItemStack(Blocks.WOODEN_SLAB, 1, BIRCH.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, JUNGLE.getMetadata()), true, true);

        addWoodRecipe(0, new ItemStack(Items.BOAT), new ItemStack(Items.SPRUCE_BOAT), false);
        addWoodRecipe(0, new ItemStack(Items.BIRCH_BOAT), new ItemStack(Items.BOAT), false);
        addWoodRecipe(0, new ItemStack(Items.JUNGLE_BOAT), new ItemStack(Items.ACACIA_BOAT), false);
        addWoodRecipe(0, new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Items.DARK_OAK_BOAT), false);
        addWoodRecipe(0, new ItemStack(Items.BIRCH_BOAT), new ItemStack(Items.JUNGLE_BOAT), false, true);

        addWoodRecipe(0, new ItemStack(Items.OAK_DOOR), new ItemStack(Items.SPRUCE_DOOR), false);
        addWoodRecipe(0, new ItemStack(Items.BIRCH_DOOR), new ItemStack(Items.OAK_DOOR), false);
        addWoodRecipe(0, new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Items.ACACIA_DOOR), false);
        addWoodRecipe(0, new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Items.DARK_OAK_DOOR), false);
        addWoodRecipe(0, new ItemStack(Items.BIRCH_DOOR), new ItemStack(Items.JUNGLE_DOOR), false, true);

        addWoodRecipe(1, new ItemStack(Blocks.PLANKS, 1, OAK.getMetadata()), new ItemStack(Blocks.PLANKS, 1, SPRUCE.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.PLANKS, 1, BIRCH.getMetadata()), new ItemStack(Blocks.PLANKS, 1, OAK.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.PLANKS, 1, JUNGLE.getMetadata()), new ItemStack(Blocks.PLANKS, 1, ACACIA.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.PLANKS, 1, SPRUCE.getMetadata()), new ItemStack(Blocks.PLANKS, 1, DARK_OAK.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.PLANKS, 1, BIRCH.getMetadata()), new ItemStack(Blocks.PLANKS, 1, JUNGLE.getMetadata()), true, true);

        addWoodRecipe(1, new ItemStack(Blocks.OAK_STAIRS), new ItemStack(Blocks.SPRUCE_STAIRS));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_STAIRS), new ItemStack(Blocks.OAK_STAIRS));
        addWoodRecipe(1, new ItemStack(Blocks.JUNGLE_STAIRS), new ItemStack(Blocks.ACACIA_STAIRS));
        addWoodRecipe(1, new ItemStack(Blocks.SPRUCE_STAIRS), new ItemStack(Blocks.DARK_OAK_STAIRS));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_STAIRS), new ItemStack(Blocks.DARK_OAK_STAIRS));

        addWoodRecipe(1, new ItemStack(Blocks.OAK_FENCE), new ItemStack(Blocks.SPRUCE_FENCE));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Blocks.OAK_FENCE));
        addWoodRecipe(1, new ItemStack(Blocks.JUNGLE_FENCE), new ItemStack(Blocks.ACACIA_FENCE));
        addWoodRecipe(1, new ItemStack(Blocks.SPRUCE_FENCE), new ItemStack(Blocks.DARK_OAK_FENCE));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_FENCE), new ItemStack(Blocks.JUNGLE_FENCE), true, true);

        addWoodRecipe(1, new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack(Blocks.SPRUCE_FENCE_GATE));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.OAK_FENCE_GATE));
        addWoodRecipe(1, new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack(Blocks.ACACIA_FENCE_GATE));
        addWoodRecipe(1, new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack(Blocks.DARK_OAK_FENCE_GATE));
        addWoodRecipe(1, new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack(Blocks.JUNGLE_FENCE_GATE), true, true);

        addWoodRecipe(1, new ItemStack(Blocks.WOODEN_SLAB, 1, OAK.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, SPRUCE.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.WOODEN_SLAB, 1, BIRCH.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, OAK.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.WOODEN_SLAB, 1, JUNGLE.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, ACACIA.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.WOODEN_SLAB, 1, SPRUCE.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, DARK_OAK.getMetadata()));
        addWoodRecipe(1, new ItemStack(Blocks.WOODEN_SLAB, 1, BIRCH.getMetadata()), new ItemStack(Blocks.WOODEN_SLAB, 1, JUNGLE.getMetadata()), true, true);

        addWoodRecipe(1, new ItemStack(Items.BOAT), new ItemStack(Items.SPRUCE_BOAT), false);
        addWoodRecipe(1, new ItemStack(Items.BIRCH_BOAT), new ItemStack(Items.BOAT), false);
        addWoodRecipe(1, new ItemStack(Items.JUNGLE_BOAT), new ItemStack(Items.ACACIA_BOAT), false);
        addWoodRecipe(1, new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Items.DARK_OAK_BOAT), false);
        addWoodRecipe(1, new ItemStack(Items.BIRCH_BOAT), new ItemStack(Items.JUNGLE_BOAT), false, true);

        addWoodRecipe(1, new ItemStack(Items.OAK_DOOR), new ItemStack(Items.SPRUCE_DOOR), false);
        addWoodRecipe(1, new ItemStack(Items.BIRCH_DOOR), new ItemStack(Items.OAK_DOOR), false);
        addWoodRecipe(1, new ItemStack(Items.JUNGLE_DOOR), new ItemStack(Items.ACACIA_DOOR), false);
        addWoodRecipe(1, new ItemStack(Items.SPRUCE_DOOR), new ItemStack(Items.DARK_OAK_DOOR), false);
        addWoodRecipe(1, new ItemStack(Items.BIRCH_DOOR), new ItemStack(Items.JUNGLE_DOOR), false, true);
    }

    private void addWoodRecipe(int type, ItemStack input, ItemStack output) {
        addWoodRecipe(type, input, output, true);
    }

    private void addWoodRecipe(int type, ItemStack input, ItemStack output, boolean multiple) {
        addWoodRecipe(type, input, output, multiple, false);
    }

    private void addWoodRecipe(int type, ItemStack input, ItemStack output, boolean multiple, boolean jungle) {
        int inputAmount = 1 + toInt(jungle) + (multiple ? 5 : 1);
        Ingredient[] inputs = new Ingredient[inputAmount];

        String name = type == 0 ? "_staining" : "_bleaching";
        ItemStack bottle = type == 0 ? getMaterial(WOOD_STAIN) : getMaterial(WOOD_BLEACH);
        if (type == 1) {
            ItemStack s = output;
            output = input;
            input = s;
        }

        Ingredient in = Ingredient.fromStacks(input);

        int setAmount = 1;

        inputs[0] = Ingredient.fromStacks(bottle);
        if (jungle) {
            inputs[1] = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, EnumDyeColor.PINK.getDyeDamage()));
            setAmount++;
        }

        while (setAmount < inputs.length) {
            inputs[setAmount] = in;
            setAmount++;
        }

        if (multiple) output.setCount(5);
        GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, Objects.requireNonNull(output.getItem().getRegistryName()).getPath() + "." + output.getMetadata() + name), null, output, inputs);
    }

    private ShapedOreRecipe lessStupidOreRecipe(ResourceLocation registryName, ItemStack result, Object... obj) {
        ShapedOreRecipe recipe = new ShapedOreRecipe(null, result, obj);
        recipe.setRegistryName(registryName);
        return recipe;
    }

    private int toInt(boolean b) {
        return b ? 1 : 0;
    }

    private ItemStack getColorItem(EnumDyeColor color) {
        return new ItemStack(Items.DYE, 1, color.getDyeDamage());
    }

    private void registerColoredRecipes(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();

        Ingredient twine = Ingredient.fromStacks(getMaterial(TWINE));

        // Plain Fabric
        ItemStack plainFabric = new ItemStack(FABRICS.get(0));
        plainFabric.setCount(2);

        registry.register(lessStupidOreRecipe(plainFabric.getItem().getRegistryName(), plainFabric, "AAA", "ABA", "AAA", 'A', twine, 'B', "stickWood"));

        // // // CUSHIONS // // //
        Ingredient cushionMiddle = Ingredient.fromItems(getItemBlock(FEATHER_BLOCK), getItemBlock(Blocks.HAY_BLOCK));

        // Plain Cushion
        ItemStack plainCushion = new ItemStack(CUSHIONS.get(0));
        plainCushion.setCount(2);

        Ingredient plainFabricIng = Ingredient.fromItem(FABRICS.get(0));

        registry.register(lessStupidOreRecipe(plainCushion.getItem().getRegistryName(), plainCushion, "AAA", "BCB", "AAA", 'A', plainFabricIng, 'B', "string", 'C', cushionMiddle));

        // Colorful Stuff
        for (int i = 1; i < 17; i++) {
            EnumDyeColor color = EnumDyeColor.byDyeDamage(i - 1);

            Item fabricItem = FABRICS.get(i);
            Item cushionItem = CUSHIONS.get(i);

            ResourceLocation fabricLocation = fabricItem.getRegistryName();
            ResourceLocation cushionLocation = cushionItem.getRegistryName();

            ItemStack fabric = new ItemStack(fabricItem);
            ItemStack fabric2 = fabric.copy(); fabric2.setCount(10);

            ItemStack cushion = new ItemStack(cushionItem);
            ItemStack cushion2 = cushion.copy(); cushion2.setCount(2);

            Ingredient fabricIng = Ingredient.fromItems(fabricItem);
            Ingredient woolIng = Ingredient.fromStacks(new ItemStack(Blocks.WOOL, 1, color.getMetadata()));
            Ingredient colorIng = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, color.getDyeDamage()));

            int whiteId = 1 + EnumDyeColor.WHITE.getDyeDamage();
            Ingredient whiteFabric = Ingredient.fromItem(FABRICS.get(whiteId));
            Ingredient whiteCushion = Ingredient.fromItem(CUSHIONS.get(whiteId));

            registry.register(lessStupidOreRecipe(fabricLocation, fabric2, "AAA", "ABA", "AAA", 'A', woolIng, 'B', "stickWood"));
            registry.register(lessStupidOreRecipe(cushionLocation, cushion2, "AAA", "BCB", "AAA", 'A', fabricIng, 'B', "string", 'C', cushionMiddle));

            if (i == whiteId) {
                GameRegistry.addShapelessRecipe(new ResourceLocation(fabricLocation + "_dying"), null, fabric.copy(), colorIng, plainFabricIng);
                GameRegistry.addShapelessRecipe(new ResourceLocation(cushionLocation + "_dying"), null, cushion, colorIng, Ingredient.fromItem(CUSHIONS.get(0)));
            }
            else {
                GameRegistry.addShapelessRecipe(new ResourceLocation(fabricLocation + "_dying"), null, fabric, colorIng, whiteFabric);
                GameRegistry.addShapelessRecipe(new ResourceLocation(cushionLocation + "_dying"), null, cushion, colorIng, whiteCushion);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        for (Item item : ITEMS) {
            if (item instanceof ModelRegistry) {
                ((ModelRegistry) item).registerModels();
            }
        }

        StateMap doubleCushionMap = new StateMap.Builder().ignore(BlockCushion.HALF).build();

        for (Block block : DOUBLE_CUSHIONS) {
            ModelLoader.setCustomStateMapper(block, doubleCushionMap);
        }
    }
}
