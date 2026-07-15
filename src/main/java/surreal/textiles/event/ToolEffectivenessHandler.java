package surreal.textiles.event;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import surreal.textiles.Textiles;
import surreal.textiles.blocks.BlockWoolSlab;
import surreal.textiles.blocks.BlockWoolStairs;

@Mod.EventBusSubscriber(modid = Textiles.MODID)
public enum ToolEffectivenessHandler {

    ;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onGetBreakSpeed(PlayerEvent.BreakSpeed event) {
        final EntityPlayer player = event.getEntityPlayer();
        final ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        final Block block = event.getState().getBlock();
        if (!(block instanceof BlockWoolSlab) && !(block instanceof BlockWoolStairs)) return;
        float modifier = stack.getDestroySpeed(Blocks.WOOL.getDefaultState());
        if (modifier <= 1F) return;
        final int eff = EnchantmentHelper.getEfficiencyModifier(player);
        if (eff > 0) {
            modifier += eff * eff + 1;
        }
        event.setNewSpeed(event.getNewSpeed() + event.getOriginalSpeed() * (modifier - 1F));
    }

}
