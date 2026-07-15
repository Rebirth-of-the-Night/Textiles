package surreal.textiles.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemSlab;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockWoolSlab;
import surreal.textiles.client.models.ModelRegistry;

public class ItemWoolSlab extends ItemSlab implements ModelRegistry {

    public ItemWoolSlab(final BlockWoolSlab singleSlab, final BlockWoolSlab doubleSlab) {
        super(singleSlab, singleSlab, doubleSlab);
    }

    @Override
    public void registerModels() {
        for (final EnumDyeColor variant : ((BlockWoolSlab) block).getDyeHalf().variants) {
            ModelLoader.setCustomModelResourceLocation(this, variant.getMetadata() % 8, new ModelResourceLocation(
                    new ResourceLocation(Textiles.MODID, "wool_slab_" + variant.getName()), "inventory"));
        }
    }

}
