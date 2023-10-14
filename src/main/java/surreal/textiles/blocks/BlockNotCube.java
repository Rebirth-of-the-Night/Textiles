package surreal.textiles.blocks;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public abstract class BlockNotCube extends Block implements IFluidloggable {

    public BlockNotCube(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        useNeighborBrightness = true;
    }

    public BlockNotCube(Material materialIn) {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    // Fluid Loggable
    @Optional.Method(modid = "fluidlogged_api")
    @Override
    public boolean isFluidValid(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Fluid fluid) {
        Block block = fluid.getBlock();
        return block.getMaterial(block.getDefaultState()) == Material.WATER;
    }
}
