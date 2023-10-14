package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.ModConfig;
import surreal.textiles.blocks.properties.PropertyAxis;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
public class BlockFeather extends Block {

    protected static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyAxis.create("axis");
    public static final AxisAlignedBB FEATHER_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);


    public BlockFeather() {
        super(Material.CLOTH);
        setDefaultState(getDefaultState().withProperty(AXIS, EnumFacing.Axis.Y));
        setHardness(0.3F).setHardness(2.0F);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.FEATHER, 9));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        float fall = (float) (1.0 - ModConfig.featherBundle.damageReduction);
        super.onFallenUpon(world, pos, entityIn, fallDistance * fall);
        if (fallDistance > ModConfig.featherBundle.breakTreshold) {
            if (!world.isRemote) world.destroyBlock(pos, true);
        }
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return FEATHER_BLOCK_AABB;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        entityIn.motionX *= ModConfig.featherBundle.walkingSpeed;
        entityIn.motionZ *= ModConfig.featherBundle.walkingSpeed;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(AXIS, facing.getAxis());
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(AXIS).ordinal();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AXIS, EnumFacing.Axis.values()[meta]);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }
}
