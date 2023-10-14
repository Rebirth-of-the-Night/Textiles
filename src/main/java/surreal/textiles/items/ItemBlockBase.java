package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import surreal.textiles.client.models.ModelRegistry;

import javax.annotation.Nonnull;

public class ItemBlockBase extends ItemBlock implements ModelRegistry {

    public ItemBlockBase(Block block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull ItemStack stack) {
        int metadata = stack.getMetadata();
        String translationKey = super.getTranslationKey(stack);

        if (metadata != 0) translationKey += "_" + metadata;

        return translationKey;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
