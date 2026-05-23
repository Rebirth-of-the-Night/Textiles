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
import surreal.textiles.ModConfig;
import surreal.textiles.RegistryManager;
import surreal.textiles.Textiles;
import surreal.textiles.items.ItemMaterial;

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

}
