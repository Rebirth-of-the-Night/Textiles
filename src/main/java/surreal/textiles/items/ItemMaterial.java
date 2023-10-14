package surreal.textiles.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.IRarity;
import surreal.textiles.client.models.ModelRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;

// Items that're only used for crafting and nothing else
public class ItemMaterial extends Item implements ModelRegistry {

    public ItemMaterial() {
        setHasSubtypes(true);
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        return super.getTranslationKey(stack) + "." + Type.VALUES[stack.getMetadata()].getName();
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < Type.VALUES.length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Nonnull
    @Override
    public IRarity getForgeRarity(@Nonnull ItemStack stack) {
        return Type.VALUES[stack.getMetadata()].getRarity();
    }

    @Override
    public void registerModels() {
        ResourceLocation location = Objects.requireNonNull(getRegistryName());

        for (int i = 0; i < Type.VALUES.length; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(location.getNamespace() + ":item/" + location.getPath(), "type=" + Type.VALUES[i].getName()));
        }
    }

    public enum Type {
        WICKER_PATCH("wicker_patch"),
        TWINE("twine"),
        FLAX_STALKS("flax_stalks"),
        PALE_FLAX_BLOSSOMS("pale_blossom"),
        VIBRANT_FLAX_BLOSSOMS("vibrant_blossom"),
        EXQUISITE_FLAX_BLOSSOMS("exquisite_blossom", EnumRarity.UNCOMMON),
        CHAIN_MESH("chain_mesh"),
        RAW_PLANT_FIBERS("plant_fibers"),
        SILK_THREAD("silk_thread"),
        SILK_WISPS("silk_wisps"),
        FLAXSEED_OIL_BOTTLE("flaxseed_oil"),
        WOOD_STAIN("wood_stain"),
        WOOD_BLEACH("wood_bleach");

        static final Type[] VALUES = values();

        private final String name;
        private final EnumRarity rarity;

        Type(String name) {
            this(name, EnumRarity.COMMON);
        }

        Type(String name, EnumRarity rarity) {
            this.name = name;
            this.rarity = rarity;
        }

        public String getName() {
            return name;
        }

        public EnumRarity getRarity() {
            return rarity;
        }
    }
}
