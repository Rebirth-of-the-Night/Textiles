package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import surreal.textiles.Textiles;
import surreal.textiles.entities.EntityBasket;
import surreal.textiles.tiles.TileBasket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
public class BlockBasket extends Block implements ITileEntityProvider {

    protected static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 1);
    protected static final PropertyDirection FACING = PropertyDirection.create("facing");

    protected static final AxisAlignedBB UP_BASKET_AABB, DOWN_BASKET_AABB, NORTH_BASKET_AABB, SOUTH_BASKET_AABB, EAST_BASKET_AABB, WEST_BASKET_AABB;
    protected static final AxisAlignedBB UP_COLLISION_AABB, DOWN_COLLISION_AABB, NORTH_COLLISION_AABB, SOUTH_COLLISION_AABB, EAST_COLLISION_AABB, WEST_COLLISION_AABB;

    public BlockBasket() {
        super(Material.WOOD);
        setDefaultState(getDefaultState().withProperty(TYPE, 0).withProperty(FACING, EnumFacing.UP));
        useNeighborBrightness = true;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(TYPE);
    }

    @Nonnull
    @Override
    public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!worldIn.isRemote && entityIn instanceof EntityItem entityItem) {
            ItemStack stack = entityItem.getItem();

            if (!stack.isEmpty()) {
                TileBasket basket = getTile(worldIn, pos);
                IItemHandler handler = basket.getInventory();

                for (int i = 0; i < handler.getSlots(); i++) {
                    entityItem.setItem(handler.insertItem(i, stack, false));
                    if (entityItem.getItem().isEmpty()) {
                        entityIn.setDead();
                        break;
                    }
                }
            }
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
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {

        if (!worldIn.isRemote) {
            TileBasket tile = getTile(worldIn, pos);

            if (tile.shouldDrop()) {
                ItemStack stack = new ItemStack(this, 1, damageDropped(state));
                NBTTagCompound tag = new NBTTagCompound();

                stack.setTagCompound(tag);
                tag.setTag("BlockEntityTag", tile.writeInventoryToNBT(new NBTTagCompound()));

                EntityBasket entity = new EntityBasket(worldIn, pos, stack);
                worldIn.spawnEntity(entity);
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (stack.hasTagCompound()) {
            TileBasket tile = getTile(worldIn, pos);
            NBTTagCompound tag = stack.getTagCompound();
            tile.readInventoryFromNBT(tag);
        }
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }

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

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = this.getStateFromMeta(placer.getHeldItem(hand).getMetadata());
        return state.withProperty(FACING, facing);
    }

    // Blockstate
    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(FACING).getIndex() * 2 + state.getValue(TYPE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(TYPE, meta & 1).withProperty(FACING, EnumFacing.byIndex(meta / 2));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE, FACING);
    }

    // Tile Entity
    private TileBasket getTile(IBlockAccess world, BlockPos pos) {
        return (TileBasket) world.getTileEntity(pos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileBasket(meta / 6);
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
}
