package surreal.textiles.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemHandlerHelper;
import surreal.textiles.Textiles;
import surreal.textiles.tiles.TileBasket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BlockBasket extends BlockContainer {

    protected static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
    protected static final PropertyDirection FACING = PropertyDirection.create("facing");

    protected static final AxisAlignedBB UP_BASKET_AABB, DOWN_BASKET_AABB, NORTH_BASKET_AABB, SOUTH_BASKET_AABB, EAST_BASKET_AABB, WEST_BASKET_AABB;
    protected static final AxisAlignedBB UP_COLLISION_AABB, DOWN_COLLISION_AABB, NORTH_COLLISION_AABB, SOUTH_COLLISION_AABB, EAST_COLLISION_AABB, WEST_COLLISION_AABB;

    public BlockBasket() {
        super(Material.WOOD);
        setSoundType(SoundType.WOOD);
        setHardness(0.5F);
        setDefaultState(getDefaultState().withProperty(TYPE, Type.DEFAULT).withProperty(FACING, EnumFacing.UP));
        useNeighborBrightness = true;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onEntityCollision(final World world, final BlockPos pos, final IBlockState state, final Entity entity) {
        if (world.isRemote || !(entity instanceof EntityItem entityItem)) return;
        final ItemStack stack = entityItem.getItem();
        if (stack.isEmpty()) return;
        switch (state.getValue(FACING)) {
            case DOWN:
                if (entity.motionY < 0D || entity.posY > pos.getY() + 0.5D) return;
                break;
            case UP:
                if (entity.motionY > 0D || entity.posY < pos.getY() + 0.5D) return;
                break;
            case NORTH:
                if (entity.motionZ < 0D || entity.posZ > pos.getZ() + 0.5D) return;
                break;
            case SOUTH:
                if (entity.motionZ > 0D || entity.posZ < pos.getZ() + 0.5D) return;
                break;
            case WEST:
                if (entity.motionX < 0D || entity.posX > pos.getZ() + 0.5D) return;
                break;
            case EAST:
                if (entity.motionX > 0D || entity.posX < pos.getZ() + 0.5D) return;
                break;
        }
        if (!(world.getTileEntity(pos) instanceof TileBasket te)) return;
        final ItemStack remStack = ItemHandlerHelper.insertItemStacked(te.getInventory(), stack, false);
        if (remStack.isEmpty()) {
            entityItem.setDead();
        } else {
            entityItem.setItem(remStack);
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) playerIn.openGui(Textiles.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {}

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (!world.isRemote) {
            if (world.getTileEntity(pos) instanceof TileBasket basket) {
                final ItemStack stack = newStack(basket.getType(), 1);
                final NBTTagCompound stackData = new NBTTagCompound();
                stackData.setTag("BlockEntityTag", basket.writeToExternalNBT(new NBTTagCompound()));
                stack.setTagCompound(stackData);
                spawnAsEntity(world, pos, stack);
            } else {
                spawnAsEntity(world, pos, newStack(state.getValue(TYPE), 1));
            }
        }
        super.breakBlock(world, pos, state);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state,
                                final EntityLivingBase placer, final ItemStack stack) {
        final NBTTagCompound stackData = stack.getTagCompound();
        if (stackData == null || !stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) return;
        if (world.getTileEntity(pos) instanceof TileBasket basket) {
            basket.readStateFromNBT(stackData.getCompoundTag("BlockEntityTag"));
        }
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for (final Type type : Type.VALUES) {
            items.add(newStack(type, 1));
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);

        return switch (facing) {
            case UP -> UP_COLLISION_AABB;
            case DOWN -> DOWN_COLLISION_AABB;
            case EAST -> EAST_COLLISION_AABB;
            case WEST -> WEST_COLLISION_AABB;
            case NORTH -> NORTH_COLLISION_AABB;
            case SOUTH -> SOUTH_COLLISION_AABB;
        };
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);

        return switch (facing) {
            case UP -> UP_BASKET_AABB;
            case DOWN -> DOWN_BASKET_AABB;
            case EAST -> EAST_BASKET_AABB;
            case WEST -> WEST_BASKET_AABB;
            case NORTH -> NORTH_BASKET_AABB;
            case SOUTH -> SOUTH_BASKET_AABB;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = this.getStateFromMeta(placer.getHeldItem(hand).getMetadata());
        return state.withProperty(FACING, facing);
    }

    // Blockstate
    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (state.getValue(FACING).getIndex() << 1) | (state.getValue(TYPE).ordinal());
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(TYPE, Type.fromMeta(meta & 0x1))
                .withProperty(FACING, EnumFacing.byIndex(meta >>> 1));
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE).ordinal();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE, FACING);
    }

    // Tile Entity
    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return TileBasket.create(Type.fromMeta(meta & 0x1));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return TileBasket.create(state.getValue(TYPE));
    }

    public ItemStack newStack(final Type type, final int count) {
        return new ItemStack(this, count, type.ordinal());
    }

    static {
        double pixel = 1D / 16;
        double pixelEnd = 1D - pixel;

        UP_BASKET_AABB = new AxisAlignedBB(pixel, 0, pixel, pixelEnd, 1, pixelEnd);
        DOWN_BASKET_AABB = new AxisAlignedBB(pixel, 0, pixel, pixelEnd, 1, pixelEnd);
        NORTH_BASKET_AABB = new AxisAlignedBB(pixel, pixel, 0, pixelEnd, pixelEnd, 1);
        SOUTH_BASKET_AABB = new AxisAlignedBB(pixel, pixel, 0, pixelEnd, pixelEnd, 1);
        WEST_BASKET_AABB = new AxisAlignedBB(0, pixel, pixel, 1, pixelEnd, pixelEnd);
        EAST_BASKET_AABB = new AxisAlignedBB(0, pixel, pixel, 1, pixelEnd, pixelEnd);

        UP_COLLISION_AABB = new AxisAlignedBB(pixel, 0, pixel, pixelEnd, pixelEnd, pixelEnd);
        DOWN_COLLISION_AABB = new AxisAlignedBB(pixel, pixel, pixel, pixelEnd, 1, pixelEnd);
        NORTH_COLLISION_AABB = new AxisAlignedBB(pixel, pixel, pixel, pixelEnd, pixelEnd, 1);
        SOUTH_COLLISION_AABB = new AxisAlignedBB(pixel, pixel, 0, pixelEnd, pixelEnd, pixelEnd);
        WEST_COLLISION_AABB = new AxisAlignedBB(pixel, pixel, pixel, 1, pixelEnd, pixelEnd);
        EAST_COLLISION_AABB = new AxisAlignedBB(0, pixel, pixel, pixelEnd, pixelEnd, pixelEnd);
    }

    public enum Type implements IStringSerializable {

        // getMetaFromState, getStateFromMeta, and createNewTileEntity depend on the number of entries here
        DEFAULT(8), STURDY(12);

        private final String lowerName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getName() {
            return lowerName;
        }

        public final int inventorySize;

        Type(final int inventorySize) {
            this.inventorySize = inventorySize;
        }

        public static final List<Type> VALUES = ImmutableList.copyOf(values());
        private static final Map<String, Type> BY_NAME = new HashMap<>();

        static {
            for (final Type type : VALUES) {
                BY_NAME.put(type.getName(), type);
            }
        }

        public static Type fromMeta(final int meta) {
            return meta >= 0 && meta < VALUES.size() ? VALUES.get(meta) : DEFAULT;
        }

        public static Type fromName(final String name) {
            final Type type = BY_NAME.get(name);
            return type != null ? type : DEFAULT;
        }

    }

}
