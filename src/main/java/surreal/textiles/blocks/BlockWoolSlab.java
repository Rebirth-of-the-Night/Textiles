package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import surreal.textiles.RegistryManager;

import java.util.Random;

public abstract class BlockWoolSlab extends BlockSlab {

    private static final EnumDyeColor[] LOWER_COLS = new EnumDyeColor[8];
    private static final EnumDyeColor[] UPPER_COLS = new EnumDyeColor[8];

    static {
        for (final EnumDyeColor col : EnumDyeColor.values()) {
            final int meta = col.getMetadata();
            if (meta >= 8) {
                UPPER_COLS[meta - 8] = col;
            } else {
                LOWER_COLS[meta] = col;
            }
        }
    }

    public static ItemStack newStack(final EnumDyeColor col, final int count) {
        final int meta = col.getMetadata();
        return new ItemStack(
                meta >= 8 ? RegistryManager.WOOL_SLAB2_HALF : RegistryManager.WOOL_SLAB1_HALF, count, meta % 8);
    }

    public BlockWoolSlab() {
        super(Material.CLOTH);
        setHardness(0.8F);
        setSoundType(SoundType.CLOTH);
    }

    @Override
    protected abstract BlockStateContainer createBlockState();

    public abstract DyeHalf getDyeHalf();

    @Override
    public PropertyEnum<EnumDyeColor> getVariantProperty() {
        return getDyeHalf().variantProperty;
    }

    @Override
    public abstract int getMetaFromState(final IBlockState state);

    @SuppressWarnings("deprecation")
    @Override
    public abstract IBlockState getStateFromMeta(final int meta);

    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return Item.getItemFromBlock(getDyeHalf().getSlabBlock());
    }

    @Override
    public int damageDropped(final IBlockState state) {
        return state.getValue(getVariantProperty()).getMetadata() % 8;
    }

    @Override
    public EnumDyeColor getTypeForItem(final ItemStack stack) {
        return getDyeHalf().getVariantByMeta(stack.getMetadata());
    }

    @Override
    public void getSubBlocks(final CreativeTabs itemIn, final NonNullList<ItemStack> items) {
        for (final EnumDyeColor variant : getDyeHalf().variants) {
            items.add(new ItemStack(this, 1, variant.getMetadata() % 8));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapColor getMapColor(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
        return MapColor.getBlockColor(state.getValue(getVariantProperty()));
    }

    @Override
    public String getTranslationKey(final int meta) {
        return super.getTranslationKey() + "." + getDyeHalf().getVariantByMeta(meta).getName();
    }

    public enum DyeHalf {
        LOWER(LOWER_COLS) {
            @Override
            public Block getSlabBlock() {
                return RegistryManager.WOOL_SLAB1_HALF;
            }
        },
        UPPER(UPPER_COLS) {
            @Override
            public Block getSlabBlock() {
                return RegistryManager.WOOL_SLAB2_HALF;
            }
        };

        public final EnumDyeColor[] variants;
        public final PropertyEnum<EnumDyeColor> variantProperty;

        DyeHalf(final EnumDyeColor[] variants) {
            this.variants = variants;
            this.variantProperty = PropertyEnum.create("color", EnumDyeColor.class, variants);
        }

        public EnumDyeColor getVariantByMeta(final int meta) {
            return (meta >= 0 && meta < variants.length) ? variants[meta] : variants[0];
        }

        public abstract Block getSlabBlock();
    }

    public static abstract class Half extends BlockWoolSlab {

        public Half() {
            setDefaultState(blockState.getBaseState().withProperty(HALF, EnumBlockHalf.BOTTOM));
        }

        @Override
        public boolean isDouble() {
            return false;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, HALF, getVariantProperty());
        }

        @Override
        public int getMetaFromState(final IBlockState state) {
            return (state.getValue(HALF) == EnumBlockHalf.BOTTOM ? 0x8 : 0)
                    | (state.getValue(getVariantProperty()).getMetadata() % 8);
        }

        @Override
        public IBlockState getStateFromMeta(final int meta) {
            return getDefaultState()
                    .withProperty(HALF, ((meta & 0x8) != 0) ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP)
                    .withProperty(getVariantProperty(), getDyeHalf().getVariantByMeta(meta & 0x7));
        }

        public static class Lower extends Half {

            @Override
            public DyeHalf getDyeHalf() {
                return DyeHalf.LOWER;
            }

        }

        public static class Upper extends Half {

            @Override
            public DyeHalf getDyeHalf() {
                return DyeHalf.UPPER;
            }

        }
    }

    public static abstract class Double extends BlockWoolSlab {
        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, getVariantProperty());
        }

        @Override
        public int getMetaFromState(final IBlockState state) {
            return state.getValue(getVariantProperty()).getMetadata() % 8;
        }

        @Override
        public IBlockState getStateFromMeta(final int meta) {
            return getDefaultState().withProperty(getVariantProperty(), getDyeHalf().getVariantByMeta(meta));
        }

        public static class Lower extends Double {

            @Override
            public DyeHalf getDyeHalf() {
                return DyeHalf.LOWER;
            }

        }

        public static class Upper extends Double {

            @Override
            public DyeHalf getDyeHalf() {
                return DyeHalf.UPPER;
            }

        }
    }

}
