package surreal.textiles.blocks;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public abstract class BlockFibers extends BlockStackable {

    protected static PropertyInteger AMOUNT = PropertyInteger.create("amount", 0, 5);

    protected static final AxisAlignedBB
        SMALL, MEDIUM;

    public BlockFibers() {
        super(Material.PLANTS);
        setHardness(0.3F).setResistance(0.3F);
        setDefaultState(getDefaultState().withProperty(getAmountProperty(), 0));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        int amount = getAmount(state);
        if (amount > 3) return FULL_BLOCK_AABB;
        else if (amount > 1) return MEDIUM;
        else return SMALL;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 30;
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 40;
    }

    // Blockstate
    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return getAmount(state);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AMOUNT, meta);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AMOUNT);
    }

    // Property
    @Override
    public PropertyInteger getAmountProperty() {
        return AMOUNT;
    }

    @Override
    public int getMaxAmount() {
        return 5;
    }

    @Override
    public int getAmount(IBlockState state) {
        return state.getValue(AMOUNT);
    }

    static {
        double oneOfThree = 1D/3;
        double twoOfThree = oneOfThree * 2;

        SMALL = new AxisAlignedBB(0, 0, 0, 1, oneOfThree, 1);
        MEDIUM = new AxisAlignedBB(0, 0, 0, 1, twoOfThree, 1);
    }
}
