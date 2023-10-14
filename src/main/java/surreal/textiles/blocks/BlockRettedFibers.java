package surreal.textiles.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.items.ItemMaterial;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class BlockRettedFibers extends BlockFibers {

    @ParametersAreNonnullByDefault
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int twineDrop = ModConfig.fibers.twineDrop;

        if (twineDrop != 0) {
            Random random = world instanceof World w ? w.rand : RANDOM;
            int amount = 0;

            if (twineDrop == 1) {
                amount = getAmount(state) + 1;
            }
            else {
                for (int i = 0; i < getAmount(state); i++) {
                    amount += MathHelper.getInt(random, 1, twineDrop);
                }
            }

            drops.add(RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.TWINE, amount));
        }
    }

    @Nonnull
    @Override
    protected ItemStack getSilkTouchDrop(@Nonnull IBlockState state) {
        return new ItemStack(this, getAmount(state));
    }
}
