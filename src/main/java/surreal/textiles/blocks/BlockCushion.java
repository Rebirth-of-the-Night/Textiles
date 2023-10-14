package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.ModConfig;
import surreal.textiles.blocks.properties.PropertyAxis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockCushion extends BlockSlabBase {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyAxis.create("axis");

    protected static final AxisAlignedBB
        X_TOP, X_BOTTOM,
        Z_TOP, Z_BOTTOM;

    public BlockCushion() {
        super(Material.CLOTH);
        setHardness(0.8F).setResistance(8.0F);
        setDefaultState(getDefaultState().withProperty(AXIS, EnumFacing.Axis.Y).withProperty(HALF, EnumBlockHalf.TOP));
        useNeighborBrightness = true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        entityIn.motionX *= ModConfig.cushions.walkingSpeed;
        entityIn.motionZ *= ModConfig.cushions.walkingSpeed;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        float fall = (float) (1.0 - ModConfig.cushions.damageReduction);
        super.onFallenUpon(world, pos, entityIn, fallDistance * fall);
    }

    @Override
    public void onLanded(@Nonnull World worldIn, @Nonnull Entity entityIn) {
        if (entityIn.isSneaking()) super.onLanded(worldIn, entityIn);
        if (entityIn.motionY < 0) {
            entityIn.motionY *= 0.8F;
        }
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        EnumBlockHalf half = state.getValue(HALF);

        if (axis == EnumFacing.Axis.Y || isDouble()) return super.getBlockFaceShape(worldIn, state, pos, face);
        else if (axis == face.getAxis()) {
            if (half == EnumBlockHalf.TOP) {
                return face.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
            }
            else return face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
        }

        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isTopSolid(@Nonnull IBlockState state) {
        return isDouble() || (state.getValue(HALF) == EnumBlockHalf.TOP && state.getValue(AXIS) == EnumFacing.Axis.Y);
    }

    // Blockstate
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AXIS, axisFromInt(meta / 2)).withProperty(HALF, halfFromInt(meta % 2));
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        int axis = state.getValue(AXIS).ordinal() * 2;
        int half = state.getValue(HALF).ordinal();

        return axis + half;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS, HALF);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(AXIS, facing.getAxis()).withProperty(HALF, facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
    }

    @SuppressWarnings("depreaction")
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        EnumBlockHalf half = state.getValue(HALF);

        if (axis == EnumFacing.Axis.Y || isDouble()) return super.getBoundingBox(state, source, pos);
        else if (axis == EnumFacing.Axis.X) {
            if (half == EnumBlockHalf.TOP) return X_TOP;
            else return X_BOTTOM;
        }
        else {
            if (half == EnumBlockHalf.TOP) return Z_TOP;
            else return Z_BOTTOM;
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        EnumBlockHalf half = state.getValue(HALF);

        if (axis == EnumFacing.Axis.Y) return super.doesSideBlockRendering(state, world, pos, face);
        else if (axis == face.getAxis()) {
            if (half == EnumBlockHalf.TOP) {
                return face.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
            }
            else return face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
        }

        return false;
    }

    // Slab
    @Override
    public boolean isDouble() {
        return false;
    }

    protected EnumFacing.Axis axisFromInt(int ordinal) {
        return switch (ordinal) {
            case 1 -> EnumFacing.Axis.Y;
            case 2 -> EnumFacing.Axis.Z;
            default -> EnumFacing.Axis.X;
        };
    }

    protected BlockSlab.EnumBlockHalf halfFromInt(int ordinal) {
        return ordinal == 0 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM;
    }

    public static class BlockCushionDouble extends BlockCushion {

        private final Block singleSlab;

        public BlockCushionDouble(Block singleSlab) {
            this.singleSlab = singleSlab;
        }

        @ParametersAreNonnullByDefault
        @Override
        public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
            return true;
        }

        @ParametersAreNonnullByDefault
        @Override
        public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
            return 40;
        }

        @ParametersAreNonnullByDefault
        @Override
        public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
            return 10;
        }

        @Nonnull
        @Override
        public Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
            return Item.getItemFromBlock(singleSlab);
        }

        @ParametersAreNonnullByDefault
        @Nonnull
        @Override
        public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
            return new ItemStack(singleSlab);
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Nonnull
        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState().withProperty(AXIS, axisFromInt(meta));
        }

        @Override
        public int getMetaFromState(@Nonnull IBlockState state) {
            return state.getValue(AXIS).ordinal();
        }

        @Nullable
        @Override
        public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
            return BlockFeather.FEATHER_BLOCK_AABB;
        }
    }

    static {
        double half = 0.5D;
        X_TOP = new AxisAlignedBB(half, 0, 0, 1, 1, 1);
        X_BOTTOM = new AxisAlignedBB(0, 0, 0, half, 1, 1);
        Z_TOP = new AxisAlignedBB(0, 0, half, 1, 1, 1);
        Z_BOTTOM = new AxisAlignedBB(0, 0, 0, 1, 1, half);
    }
}
