package surreal.textiles.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import surreal.textiles.ModConfig;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockSpindle;
import surreal.textiles.tiles.TileSpindle;

import javax.annotation.Nonnull;

public class ItemBlockSpindle extends ItemBlockBase {

    public ItemBlockSpindle(final BlockSpindle spindle) {
        super(spindle);
        setHasSubtypes(true);
    }

    @Override
    public void registerModels() {
        for (final BlockSpindle.Type type : BlockSpindle.Type.VALUES) {
            ModelLoader.setCustomModelResourceLocation(this, type.ordinal(), new ModelResourceLocation(
                    new ResourceLocation(Textiles.MODID, "spindle_" + type.getName()), "inventory"));
        }
    }

    @Override
    public int getItemBurnTime(final ItemStack itemStack) {
        return ModConfig.fabric.fuelAmount;
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos,
                                      final EnumHand hand, final EnumFacing facing,
                                      final float hitX, final float hitY, final float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) return EnumActionResult.FAIL;

        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }
        if (!player.canPlayerEdit(pos, facing, stack) || !world.mayPlace(block, pos, false, facing, player)) {
            return EnumActionResult.FAIL;
        }

        final IBlockState state = block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, stack.getMetadata(), player, hand);
        if (placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, state)) {
            if (world.getTileEntity(pos) instanceof TileSpindle te) {
                te.placeSpindles(player, stack);
            } else { // this really shouldn't happen
                world.playSound(player, pos, SoundEvents.BLOCK_CLOTH_PLACE, SoundCategory.BLOCKS, 1F, 0.8F);
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public String getTranslationKey(@Nonnull final ItemStack stack) {
        final BlockSpindle.Type type = BlockSpindle.Type.fromMeta(stack.getMetadata());
        if (type == BlockSpindle.Type.PLAIN) return block.getTranslationKey();
        return block.getTranslationKey() + "_" + type.getName();
    }

}
