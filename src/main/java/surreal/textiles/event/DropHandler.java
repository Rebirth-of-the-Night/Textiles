package surreal.textiles.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockFlax;
import surreal.textiles.items.ItemMaterial;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Textiles.MODID)
public enum DropHandler {

    ;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);

        ItemStack stack = event.getHarvester() != null ? event.getHarvester().getHeldItemMainhand() : ItemStack.EMPTY;

        List<ItemStack> drops = event.getDrops();

        if (!event.isSilkTouching()) {
            if (state.getBlock() == Blocks.TALLGRASS && world.rand.nextFloat() < ModConfig.drops.plantFibersDrop) {
                drops.add(RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.RAW_PLANT_FIBERS));
            }
            else if (ModConfig.drops.replaceCobwebDrop && state.getBlock() == Blocks.WEB && stack.getItem() instanceof ItemSword) {
                drops.clear();
                drops.add(RegistryManager.INSTANCE.getMaterial(ItemMaterial.Type.SILK_WISPS));
            }
        }
    }

    private static final List<Pair<World, BlockPos>> shearedFlax = new ArrayList<>();

    public static void queueShearedFlax(final World world, final BlockPos pos) {
        shearedFlax.add(Pair.of(world, pos.toImmutable()));
    }

    @SubscribeEvent
    public static void onServerTickEnd(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || shearedFlax.isEmpty()) return;
        try {
            final BlockFlax flaxBlock = RegistryManager.FLAX_CROP;
            final IBlockState flaxState = flaxBlock.withAge(flaxBlock.getMaxAge() - 1)
                    .withProperty(BlockFlax.BOTTOM, false);
            for (final Pair<World, BlockPos> entry : shearedFlax) {
                final World world = entry.getLeft();
                final BlockPos pos = entry.getRight();
                if (world.isAirBlock(pos)) {
                    world.setBlockState(pos, flaxState, 11);
                }
            }
        } finally {
            shearedFlax.clear();
        }
    }

}
