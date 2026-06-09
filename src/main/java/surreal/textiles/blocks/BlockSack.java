package surreal.textiles.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import surreal.textiles.ModConfig;
import surreal.textiles.Textiles;
import surreal.textiles.entities.EntityFallingSack;
import surreal.textiles.items.ItemBlockSack;
import surreal.textiles.tiles.TileSack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockSack extends BlockContainer {

    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyBool DYED = PropertyBool.create("dyed");

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D);

    private static boolean allowDrops = true;

    public BlockSack() {
        super(Material.CLOTH);
        setSoundType(SoundType.CLOTH);
        setHardness(0.4F);
        setDefaultState(blockState.getBaseState().withProperty(OPEN, false).withProperty(DYED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, OPEN, DYED);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return 0;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new TileSack();
    }

    @Override
    public void onBlockAdded(final World world, final BlockPos pos, final IBlockState state) {
        world.scheduleUpdate(pos, this, tickRate(world));
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state,
                                final EntityLivingBase placer, final ItemStack stack) {
        final NBTTagCompound stackData = stack.getTagCompound();
        if (stackData == null || !stackData.hasKey("BlockEntityTag", Constants.NBT.TAG_COMPOUND)) return;
        if (world.getTileEntity(pos) instanceof TileSack sack) {
            sack.readStateFromNBT(stackData.getCompoundTag("BlockEntityTag"));
        }
    }

    @Override
    public ItemStack getPickBlock(final IBlockState state, final RayTraceResult target, final World world,
                                  final BlockPos pos, final EntityPlayer player) {
        if (world.getTileEntity(pos) instanceof TileSack sack) {
            final int col = sack.getDyeColor();
            if (col >= 0) {
                final ItemStack stack = new ItemStack(this, 1, 1);
                final NBTTagCompound stackData = new NBTTagCompound();
                final NBTTagCompound tileData = new NBTTagCompound();
                tileData.setInteger("dye", col);
                stackData.setTag("BlockEntityTag", tileData);
                stack.setTagCompound(stackData);
                return stack;
            }
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos,
                                final Block block, final BlockPos fromPos) {
        world.scheduleUpdate(pos, this, tickRate(world));
    }

    @Override
    public int tickRate(final World world) {
        return 2;
    }

    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random rand) {
        if (world.isRemote || pos.getY() < 0 || !ModConfig.sack.gravity) return;
        final BlockPos below = pos.down();
        if (!canFallThrough(world, below)) return;

        if (!BlockFalling.fallInstantly && world.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
            final EntityFallingSack entity = new EntityFallingSack(
                    world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
            final float damage = (float) ModConfig.sack.gravityDamage;
            if (world.getTileEntity(pos) instanceof TileSack sack) {
                entity.tileEntityData = sack.writeToNBT(new NBTTagCompound());
                entity.setDyeColor(sack.getDyeColor());
                if (damage > 0F) {
                    entity.setHurtEntities(true);
                    entity.fallHurtAmount = damage * sack.computeDamageMultiplier();
                }
            } else if (damage > 0F) {
                entity.setHurtEntities(true);
                entity.fallHurtAmount = damage;
            }
            entity.fallTime = 1; // prevent the entity from breaking the block
            allowDrops = false;
            try {
                world.setBlockToAir(pos); // break the block here instead to prevent the item from dropping
            } finally {
                allowDrops = true;
            }
            world.spawnEntity(entity);
        } else {
            BlockPos floorPos = below;
            while (floorPos.getY() > 0 && canFallThrough(world, floorPos)) {
                floorPos = floorPos.down();
            }
            if (floorPos.getY() > 0) {
                final BlockPos newPos = floorPos.up();
                final TileEntity tile = world.getTileEntity(pos);
                world.setBlockToAir(pos);
                world.setBlockState(newPos, state);
                if (tile != null) {
                    world.setTileEntity(newPos, tile);
                }
            }
        }
    }

    private static boolean canFallThrough(final World world, final BlockPos pos) {
        return world.isAirBlock(pos) || BlockFalling.canFallThrough(world.getBlockState(pos));
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
                                    final EntityPlayer player, final EnumHand hand, final EnumFacing facing,
                                    final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            player.openGui(Textiles.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess world, final BlockPos pos,
                         final IBlockState state, final int fortune) {}

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (allowDrops && !world.isRemote) {
            if (world.getTileEntity(pos) instanceof TileSack sack) {
                final ItemStack stack = new ItemStack(this, 1, sack.getDyeColor() >= 0 ? 1 : 0);
                final NBTTagCompound stackData = new NBTTagCompound();
                stackData.setTag("BlockEntityTag", sack.writeToExternalNBT(new NBTTagCompound()));
                stack.setTagCompound(stackData);
                spawnAsEntity(world, pos, stack);
            } else {
                spawnAsEntity(world, pos, new ItemStack(this));
            }
        }
        super.breakBlock(world, pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumPushReaction getPushReaction(final IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        if (world.getTileEntity(pos) instanceof TileSack sack) {
            return state.withProperty(OPEN, sack.isOpen()).withProperty(DYED, sack.getDyeColor() >= 0);
        }
        return state;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos) {
        return BOUNDING_BOX;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess world, final IBlockState state, final BlockPos pos,
                                            final EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> tooltip,
                               final ITooltipFlag flags) {
        final int col = ItemBlockSack.getDyeColorForStack(stack);
        if (col >= 0) {
            if (flags.isAdvanced()) {
                tooltip.add(I18n.format("item.color", String.format("#%06X", col)));
            } else {
                tooltip.add(TextFormatting.ITALIC + I18n.format("item.dyed"));
            }
        }
    }

}
