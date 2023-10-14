package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import surreal.textiles.ModConfig;
import surreal.textiles.blocks.BlockFibers;
import surreal.textiles.blocks.BlockSpindle;
import surreal.textiles.blocks.BlockStackable;
import surreal.textiles.client.models.ModelRegistry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemBlockStackable extends ItemBlock implements ModelRegistry {

    public ItemBlockStackable(BlockStackable block) {
        super(block);
    }

    @Override
    public int getItemBurnTime(@Nonnull ItemStack stack) {
        if (block instanceof BlockFibers) return ModConfig.fibers.fuelAmount;
        else if (block instanceof BlockSpindle) return ModConfig.fabric.fuelAmount;

        return super.getItemBurnTime(stack);
    }

    @ParametersAreNonnullByDefault
    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block instanceof BlockStackable stackable && stackable.getAmount(iblockstate) < stackable.getMaxAmount()) return EnumActionResult.PASS;

        if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        ItemStack itemstack = player.getHeldItem(hand);

        if (!itemstack.isEmpty() && player.canPlayerEdit(pos, facing, itemstack) && worldIn.mayPlace(this.block, pos, false, facing, player)) {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);
            BlockStackable stackable = (BlockStackable) this.block;

            int amount = 1;

            if (player.isSneaking()) {
                amount = player.isCreative() ? stackable.getMaxAmount() + 1 : Math.min(stackable.getMaxAmount() + 1, itemstack.getCount());
                iblockstate1 = iblockstate1.withProperty(stackable.getAmountProperty(), amount - 1);
            }

            if (placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1)) {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(amount);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}
