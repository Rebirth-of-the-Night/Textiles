package surreal.textiles.blocks;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.tiles.TileRawFibers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockRawFibers extends BlockFibers implements ITileEntityProvider {

    protected static PropertyInteger AGE = PropertyInteger.create("age", 0, 2);

    public BlockRawFibers() {
        setDefaultState(getDefaultState().withProperty(AGE, 0));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AMOUNT, AGE);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextFloat() < ModConfig.fibers.updateChance) {
            int age = state.getValue(AGE);
            int amount = getAmount(state);

            if (age != 2) {
                worldIn.setBlockState(pos, state.withProperty(AGE, age + 1));
                getTile(worldIn, pos).setAge(age + 1);
                scheduleUpdate(worldIn, pos);
            }
            else {
                worldIn.setBlockState(pos, RegistryManager.DRIED_FIBERS.getDefaultState().withProperty(AMOUNT, amount));
            }
        }
        else scheduleUpdate(worldIn, pos);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (world.isUpdateScheduled(pos, this)) return;

        TileRawFibers tile = getTile(world, pos);
        tile.setAge(state.getValue(AGE));

        if (Loader.isModLoaded("fluidlogged_api")) {
            if (FluidloggedUtils.getFluidState(world, pos) != FluidState.EMPTY) {
                scheduleUpdate(world, pos);
            }
        } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos nPos = pos.offset(facing);
                IBlockState nState = world.getBlockState(nPos);
                Block nBlock = nState.getBlock();

                if ((nBlock instanceof IFluidBlock || nBlock instanceof BlockLiquid) && nBlock.getMaterial(nState) == Material.WATER) {
                    scheduleUpdate(world, pos);
                }
            }
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (Loader.isModLoaded("fluidlogged_api") || world.isUpdateScheduled(pos, this)) return;

        IBlockState nState = world.getBlockState(fromPos);
        Block nBlock = nState.getBlock();

        if ((nBlock instanceof IFluidBlock || nBlock instanceof BlockLiquid) && nBlock.getMaterial(nState) == Material.WATER) {
            scheduleUpdate(world, pos);
        }
    }

    // Fluid Logging
    @Optional.Method(modid = "fluidlogged_api")
    @Nonnull
    @Override
    public EnumActionResult onFluidChange(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState here, @Nonnull FluidState newFluid, int blockFlags) {
        if (newFluid != FluidState.EMPTY) scheduleUpdate(world, pos);
        return super.onFluidChange(world, pos, here, newFluid, blockFlags);
    }

    // Update
    protected void scheduleUpdate(World world, BlockPos pos) {
        world.scheduleBlockUpdate(pos, this, 20 * ModConfig.fibers.updateDelay, 0);
    }

    // Tile Entity
    public TileRawFibers getTile(IBlockAccess world, BlockPos pos) {
        return (TileRawFibers) world.getTileEntity(pos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileRawFibers();
    }
}
