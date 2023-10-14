package surreal.textiles.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.items.ItemBlockStackable;

import javax.annotation.ParametersAreNonnullByDefault;

public abstract class BlockStackable extends BlockNotCube {

    public BlockStackable(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        setDefaultState(getDefaultState().withProperty(getAmountProperty(), 0));
    }

    public BlockStackable(Material materialIn) {
        super(materialIn, materialIn.getMaterialMapColor());
    }

    @ParametersAreNonnullByDefault
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(this, getMetaFromState(state) + 1));
    }

    @ParametersAreNonnullByDefault
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        int meta = getMetaFromState(state);

        if (meta < getMaxAmount()) {
            ItemStack stack = player.getHeldItem(hand);

            if (stack.getItem() instanceof ItemBlockStackable) {
                state = state.withProperty(getAmountProperty(), meta + 1);

                world.setBlockState(pos, state, 3);

                SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!player.isCreative()) stack.shrink(1);

                return true;
            }
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }


    public abstract PropertyInteger getAmountProperty();
    public abstract int getMaxAmount();
    public abstract int getAmount(IBlockState state);
}
