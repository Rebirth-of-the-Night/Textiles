package surreal.textiles.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.blocks.properties.PropertyAxis;
import surreal.textiles.tiles.TileSpindle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
public class BlockSpindle extends BlockStackable implements ITileEntityProvider {

    public static PropertyInteger AMOUNT = PropertyInteger.create("amount", 0, 8);
    public static PropertyEnum<EnumFacing.Axis> AXIS = PropertyAxis.create("axis", EnumFacing.Axis.X, EnumFacing.Axis.Z);

    protected static final AxisAlignedBB
            SMALL_X, MEDIUM_X, LARGE_X,
            SMALL_Z, MEDIUM_Z, LARGE_Z;

    public BlockSpindle() {
        super(Material.CLOTH);
        setDefaultState(getDefaultState().withProperty(AXIS, EnumFacing.Axis.X));
        setHardness(0.08F).setResistance(2F);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @ParametersAreNonnullByDefault
    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 30;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        int amount = getMetaFromState(state);
        EnumFacing.Axis axis = state.getValue(AXIS);

        if (amount < 3) {
            if (axis == EnumFacing.Axis.X) return SMALL_X;
            else return SMALL_Z;
        }
        else if (amount < 6) {
            if (axis == EnumFacing.Axis.X) return MEDIUM_X;
            else return MEDIUM_Z;
        }
        else {
            if (axis == EnumFacing.Axis.X) return LARGE_X;
            else return LARGE_Z;
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        int meta = getMetaFromState(state);

        if (meta < 8) {
            ItemStack stack = player.getHeldItem(hand);

            if (stack.getItem() == Item.getItemFromBlock(this)) {
                EnumFacing.Axis axis = state.getValue(AXIS);

                state = state.withProperty(getAmountProperty(), meta + 1);

                world.setBlockState(pos, state, 3);

                TileSpindle spindle = getTile(world, pos);
                spindle.setAxis(axis);

                SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!player.isCreative()) stack.shrink(1);

                return true;
            }
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);

        EnumFacing.Axis axis = placer.getHorizontalFacing().getAxis();
        if (facing.getAxis().isHorizontal()) axis = facing.getAxis();

        return state.withProperty(AXIS, axis);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileSpindle spindle = getTile(worldIn, pos);
        spindle.setAxis(state.getValue(AXIS));
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        int amount = getMetaFromState(worldIn.getBlockState(pos));
        if (amount < 3) return BlockFaceShape.UNDEFINED;
        else if (amount < 5) return BlockFaceShape.UNDEFINED;

        EnumFacing.Axis axis = state.getValue(AXIS);

        if (axis == EnumFacing.Axis.X) {
            return face.getAxis() == EnumFacing.Axis.Z ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
        }
        else return face.getAxis() == EnumFacing.Axis.X ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileSpindle spindle = getTile(worldIn, pos);
        return state.withProperty(AXIS, spindle.getAxis());
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(AMOUNT);
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
        return new BlockStateContainer(this, AMOUNT, AXIS);
    }

    // Property
    @Override
    public PropertyInteger getAmountProperty() {
        return AMOUNT;
    }

    @Override
    public int getMaxAmount() {
        return 8;
    }

    @Override
    public int getAmount(IBlockState state) {
        return state.getValue(AMOUNT);
    }

    // Static initializer
    static {
        double small = 1D/3;
        double two = 1D/8;
        double two2 = two * 7;

        SMALL_Z = new AxisAlignedBB(0, 0, two, 1, small, two2);
        MEDIUM_Z = new AxisAlignedBB(0, 0, two, 1, small * 2, two2);
        LARGE_Z = new AxisAlignedBB(0, 0, two, 1, 1, two2);

        SMALL_X = new AxisAlignedBB(two, 0, 0, two2, small, 1);
        MEDIUM_X = new AxisAlignedBB(two, 0, 0, two2, small * 2, 1);
        LARGE_X = new AxisAlignedBB(two, 0, 0, two2, 1, 1);
    }

    // Tile Entity
    private TileSpindle getTile(IBlockAccess world, BlockPos pos) {
        return (TileSpindle) world.getTileEntity(pos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileSpindle();
    }
}
