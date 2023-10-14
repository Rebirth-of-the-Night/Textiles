package surreal.textiles.blocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class BlockSlabBase extends BlockSlab {

    public BlockSlabBase(Material materialIn) {
        super(materialIn);
    }

    public BlockSlabBase(Material p_i47249_1_, MapColor p_i47249_2_) {
        super(p_i47249_1_, p_i47249_2_);
    }

    @Deprecated
    @Nonnull
    @Override
    public String getTranslationKey(int meta) {
        return getTranslationKey();
    }

    @Deprecated
    @Nonnull
    @Override
    public IProperty<?> getVariantProperty() {
        return HALF;
    }

    @Deprecated
    @Nonnull
    @Override
    public Comparable<?> getTypeForItem(@Nonnull ItemStack stack) {
        return 31;
    }
}
