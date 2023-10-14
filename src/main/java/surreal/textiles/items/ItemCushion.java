package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import surreal.textiles.blocks.BlockCushion;
import surreal.textiles.client.models.ModelRegistry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemCushion extends ItemBlock implements ModelRegistry {

    protected final BlockSlab doubleSlab;

    public ItemCushion(BlockSlab block, BlockSlab singleSlab) {
        super(singleSlab);
        this.doubleSlab = block;
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty()) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if (player.canPlayerEdit(pos, facing, itemstack) && shouldMakeDouble(iblockstate, facing) && tryPlace(worldIn, pos, player, itemstack)) return EnumActionResult.SUCCESS;
            else if (player.canPlayerEdit(pos.offset(facing), facing, itemstack) && canPlaceBlockOnSide(worldIn, pos, facing, player, itemstack)) return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }

        return EnumActionResult.FAIL;
    }

    // Runs when shouldMakeDouble returns false
    @ParametersAreNonnullByDefault
    @SideOnly(Side.CLIENT)
    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        IBlockState state = worldIn.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.SNOW_LAYER || block.isReplaceable(worldIn, pos)) side = EnumFacing.UP;
        else if (!block.isReplaceable(worldIn, pos)) pos = pos.offset(side);

        return worldIn.mayPlace(this.block, pos, false, side, player);
    }

    private boolean shouldMakeDouble(IBlockState state, EnumFacing side) {
        Block block = state.getBlock();
        if (block != this.block) return false;

        EnumFacing.Axis axis = state.getValue(BlockCushion.AXIS);
        BlockSlab.EnumBlockHalf half = state.getValue(BlockCushion.HALF);

        return side.getAxis() == axis && (half == BlockSlab.EnumBlockHalf.TOP ? side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE : side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE);
    }

    // Runs when shouldMakeDouble returns true
    private boolean tryPlace(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        IBlockState state = world.getBlockState(pos); // This is a single slab

        IBlockState doubleSlab = this.doubleSlab.getDefaultState();
        SoundType sound = this.block.getSoundType(state, world, pos, player);

        AxisAlignedBB aabb = state.getCollisionBoundingBox(world, pos);

        if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, doubleSlab.withProperty(BlockCushion.AXIS, state.getValue(BlockCushion.AXIS)), 11)) {
            world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            stack.shrink(1);
            return true;
        }

        return false;
    }
}
