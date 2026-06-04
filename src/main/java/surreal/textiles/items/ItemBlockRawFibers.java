package surreal.textiles.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockFibers;
import surreal.textiles.blocks.BlockStackable;
import surreal.textiles.compat.TextilesCompat;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ItemBlockRawFibers extends ItemBlockStackable {

    public ItemBlockRawFibers(final BlockFibers block) {
        super(block);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos,
                                      final EnumHand hand, final EnumFacing facing,
                                      final float hitX, final float hitY, final float hitZ) {
        if (tryWaterPlacement(world, player, player.getHeldItem(hand), hand, false)) {
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final RayTraceResult trace = Textiles.proxy.getKnownRayTrace(player);
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS) {
            return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        final ItemStack stack = player.getHeldItem(hand);
        final boolean result = tryWaterPlacement(world, player, stack, hand, true);
        return new ActionResult<>(result ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
    }

    private boolean tryWaterPlacement(final World world, final EntityPlayer player, final ItemStack stack,
                                      final EnumHand hand, final boolean handleCustomPlacement) {
        // only use water lily placement behaviour if fluidlogged api isn't present
        if (TextilesCompat.FLUIDLOGGED_LOADED) return false;

        final RayTraceResult trace = rayTrace(world, player, true); // erroneously marked non-null
        //noinspection ConstantValue
        if (trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK) return false;

        final BlockPos hitPos = trace.getBlockPos();
        if (!world.isBlockModifiable(player, hitPos)) return false;
        if (!player.canPlayerEdit(hitPos.offset(trace.sideHit), trace.sideHit, stack)) return false;

        final IBlockState hitState = world.getBlockState(hitPos);
        if (hitState.getMaterial() != Material.WATER || hitState.getValue(BlockLiquid.LEVEL) != 0) return false;

        final BlockPos placePos = hitPos.up();
        if (!world.isAirBlock(placePos)) return false;

        final BlockStackable block = (BlockStackable) this.block;
        final int amount = getAmountToPlace(block, player, stack);
        final IBlockState placeState = block.getDefaultState().withProperty(block.getAmountProperty(), amount - 1);
        if (handleCustomPlacement) {
            final BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, placePos);
            world.setBlockState(placePos, placeState);
            if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, EnumFacing.UP, hand).isCanceled()) {
                snapshot.restore(true, false);
                return false;
            }
            world.markAndNotifyBlock(placePos, null, snapshot.getReplacedBlock(), placeState, 11);
        } else {
            world.setBlockState(placePos, placeState, 11);
        }
        block.onBlockPlacedBy(world, placePos, placeState, player, stack);

        if (player instanceof EntityPlayerMP mpPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(mpPlayer, placePos, stack);
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(amount);
        }
        if (handleCustomPlacement) {
            player.addStat(Objects.requireNonNull(StatList.getObjectUseStats(this)));
        }
        world.playSound(player, placePos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1F, 0.8F);
        return true;
    }

}
