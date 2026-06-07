package surreal.textiles.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.Pair;
import surreal.textiles.blocks.properties.PropertyAxis;
import surreal.textiles.tiles.TileSpindle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("deprecation")
public class BlockSpindle extends BlockNotCube implements ITileEntityProvider {

    public static final int MAX_AMOUNT = 9;

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyAxis.create("axis", EnumFacing.Axis.X, EnumFacing.Axis.Z);
    public static final PropertyInteger AMOUNT = PropertyInteger.create("amount", 1, MAX_AMOUNT);
    public static final IUnlistedProperty<List<Type>> SPINDLES = new IUnlistedProperty<>() {
        @Override
        public String getName() {
            return "spindles";
        }

        @Override
        public boolean isValid(final List<Type> value) {
            final int size = value.size();
            return size > 0 && size <= MAX_AMOUNT;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<List<Type>> getType() {
            return (Class<List<Type>>) (Class<?>) List.class;
        }

        @Override
        public String valueToString(final List<Type> value) {
            if (value.isEmpty()) return "[]";
            final StringBuilder buf = new StringBuilder("[");
            final Iterator<Type> iter = value.iterator();
            buf.append(iter.next().getName());
            while (iter.hasNext()) {
                buf.append(',').append(iter.next().getName());
            }
            return buf.toString();
        }
    };

    protected static final AxisAlignedBB
            SMALL_X, MEDIUM_X, LARGE_X,
            SMALL_Z, MEDIUM_Z, LARGE_Z;

    public BlockSpindle() {
        super(Material.CLOTH);
        setSoundType(SoundType.CLOTH);
        setDefaultState(getDefaultState().withProperty(AXIS, EnumFacing.Axis.X).withProperty(AMOUNT, 1));
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
        if (!(source.getTileEntity(pos) instanceof TileSpindle te)) return SMALL_X;
        int amount = te.getSpindles().size();
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

    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos, final IBlockState state,
                                          final float chance, final int fortune) {}

    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final IBlockState state,
                                 final EntityPlayer player) {
        if (!player.capabilities.isCreativeMode) {
            dropSpindleItems(world, pos);
        }
        world.removeTileEntity(pos); // prevent breakBlock from dropping the items again
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        dropSpindleItems(world, pos);
        super.breakBlock(world, pos, state);
    }

    private void dropSpindleItems(final World world, final BlockPos pos) {
        if (!(world.getTileEntity(pos) instanceof TileSpindle te)) return;
        final List<Type> spindles = te.getSpindles();
        final Type[] dropTypes = new Type[spindles.size()];
        final int[] dropCounts = new int[dropTypes.length];
        for (final Type type : spindles) {
            for (int i = 0; i < dropTypes.length; i++) {
                if (dropTypes[i] == type) {
                    dropCounts[i]++;
                    break;
                } else if (dropTypes[i] == null) {
                    dropTypes[i] = type;
                    dropCounts[i] = 1;
                    break;
                }
            }
        }
        for (int i = 0; i < dropTypes.length; i++) {
            final Type type = dropTypes[i];
            if (type == null) break;
            spawnAsEntity(world, pos, newStack(type, dropCounts[i]));
        }
    }

    @Override
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess world, final BlockPos pos,
                         final IBlockState state, final int fortune) {
        if (world.getTileEntity(pos) instanceof TileSpindle te) {

        }
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target,
                                  final World world, final BlockPos pos, final EntityPlayer player) {
        if (!(world.getTileEntity(pos) instanceof TileSpindle te)) {
            return super.getPickBlock(state, target, world, pos, player);
        }
        return newStack(te.getSpindles().get(0), 1);
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return world.getTileEntity(pos) instanceof TileSpindle te && te.handleInteraction(player, hand);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        final EnumFacing.Axis placeAxis = facing.getAxis();
        return getDefaultState()
                .withProperty(AXIS, placeAxis.isHorizontal() ? placeAxis : placer.getHorizontalFacing().getAxis());
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (!(worldIn.getTileEntity(pos) instanceof TileSpindle spindle)) return state;
        return state.withProperty(AMOUNT, spindle.getSpindles().size());
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        if (!(world.getTileEntity(pos) instanceof TileSpindle te)) return state;
        return ((IExtendedBlockState) state).withProperty(SPINDLES, te.getSpindles());
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(AXIS) == EnumFacing.Axis.Z ? 1 : 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return getDefaultState().withProperty(AXIS, meta == 1 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(AXIS, AMOUNT).add(SPINDLES).build();
    }

    @Override
    public void getSubBlocks(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        for (final Type type : Type.VALUES) {
            items.add(newStack(type, 1));
        }
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
    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileSpindle();
    }

    public ItemStack newStack(final Type type, final int count) {
        return new ItemStack(this, count, type.ordinal());
    }

    public enum Type implements IStringSerializable {

        PLAIN,
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        SILVER,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK,
        CANVAS;

        private final String lowerName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getName() {
            return lowerName;
        }

        public static final List<Type> VALUES = ImmutableList.copyOf(values());
        private static final Map<String, Type> BY_NAME = new HashMap<>();

        static {
            for (final Type type : VALUES) {
                BY_NAME.put(type.getName(), type);
            }
        }

        public static Type fromMeta(final int meta) {
            return meta >= 0 && meta < VALUES.size() ? VALUES.get(meta) : PLAIN;
        }

        public static Type fromName(final String name) {
            final Type type = BY_NAME.get(name);
            return type != null ? type : PLAIN;
        }

        public static Type fromDye(final EnumDyeColor col) {
            return switch (col) {
                case WHITE -> WHITE;
                case ORANGE -> ORANGE;
                case MAGENTA -> MAGENTA;
                case LIGHT_BLUE -> LIGHT_BLUE;
                case YELLOW -> YELLOW;
                case LIME -> LIME;
                case PINK -> PINK;
                case GRAY -> GRAY;
                case SILVER -> SILVER;
                case CYAN -> CYAN;
                case PURPLE -> PURPLE;
                case BLUE -> BLUE;
                case BROWN -> BROWN;
                case GREEN -> GREEN;
                case RED -> RED;
                case BLACK -> BLACK;
            };
        }

    }
}
