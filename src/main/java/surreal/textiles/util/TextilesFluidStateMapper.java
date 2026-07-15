package surreal.textiles.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidBlock;
import surreal.textiles.Textiles;

import java.util.HashMap;
import java.util.Map;

public enum TextilesFluidStateMapper implements IStateMapper {

    INSTANCE;

    private static final ResourceLocation STATE_MAP_LOC = new ResourceLocation(Textiles.MODID, "fluid");

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(final Block block) {
        if (!(block instanceof IFluidBlock fb)) {
            throw new IllegalArgumentException("Not a fluid block: " + block.getRegistryName());
        }
        final ModelResourceLocation loc = new ModelResourceLocation(STATE_MAP_LOC, fb.getFluid().getName());
        final Map<IBlockState, ModelResourceLocation> mapping = new HashMap<>();
        for (final IBlockState state : block.getBlockState().getValidStates()) {
            mapping.put(state, loc);
        }
        return mapping;
    }

}
