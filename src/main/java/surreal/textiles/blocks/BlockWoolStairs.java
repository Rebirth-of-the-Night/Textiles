package surreal.textiles.blocks;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class BlockWoolStairs extends BlockStairs {

    public BlockWoolStairs(final EnumDyeColor variant) {
        super(Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, variant));
        useNeighborBrightness = true;
    }

}
