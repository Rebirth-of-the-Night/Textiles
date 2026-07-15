package surreal.textiles.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import surreal.textiles.Textiles;
import surreal.textiles.client.models.ModelRegistry;
import surreal.textiles.entities.EntityWoolCushion;

public class ItemWoolCushion extends Item implements ModelRegistry {

    public ItemWoolCushion() {
        setHasSubtypes(true);
    }

    public ItemStack newStack(final EnumDyeColor variant, final int count) {
        return new ItemStack(this, count, variant.getMetadata());
    }

    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (final EnumDyeColor variant : EnumDyeColor.values()) {
                items.add(newStack(variant, 1));
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos,
                                      final EnumHand hand, final EnumFacing facing,
                                      final float hitX, final float hitY, final float hitZ) {
        if (facing != EnumFacing.UP) return EnumActionResult.FAIL;
        final ItemStack stack = player.getHeldItem(hand);
        final EntityWoolCushion entity = new EntityWoolCushion(
                EnumDyeColor.byMetadata(stack.getMetadata()), world, pos, hitY);
        if (!entity.isSupported()) return EnumActionResult.FAIL;

        stack.shrink(1);
        if (!world.isRemote) {
            entity.playSound(SoundEvents.BLOCK_CLOTH_PLACE, 1F, 1F);
            world.spawnEntity(entity);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void registerModels() {
        for (final EnumDyeColor variant : EnumDyeColor.values()) {
            ModelLoader.setCustomModelResourceLocation(this, variant.getMetadata(), new ModelResourceLocation(
                    new ResourceLocation(Textiles.MODID, "small_cushion_" + variant.getName()), "inventory"));
        }
    }

    @Override
    public String getTranslationKey(final ItemStack stack) {
        return super.getTranslationKey(stack) + "." + EnumDyeColor.byMetadata(stack.getMetadata()).getName();
    }

}
