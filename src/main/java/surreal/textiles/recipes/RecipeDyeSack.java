package surreal.textiles.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.DyeUtils;
import net.minecraftforge.registries.IForgeRegistryEntry;
import surreal.textiles.items.ItemBlockSack;

import java.util.Optional;

public class RecipeDyeSack extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(final InventoryCrafting inv, final World world) {
        final int size = inv.getWidth() * inv.getHeight();
        boolean seenSack = false, seenDye = false;
        for (int i = 0; i < size; i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ItemBlockSack) {
                if (seenSack) {
                    return false; // more than one sack present
                } else {
                    seenSack = true;
                }
            } else if (DyeUtils.isDye(stack)) {
                seenDye = true;
            } else {
                return false;
            }
        }
        return seenSack && seenDye;
    }

    @Override
    public ItemStack getCraftingResult(final InventoryCrafting inv) {
        final int size = inv.getWidth() * inv.getHeight();
        ItemStack sack = null;
        int sackSlot = -1;
        for (int i = 0; i < size; i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlockSack) {
                sack = stack;
                sackSlot = i;
                break;
            }
        }
        if (sack == null) return ItemStack.EMPTY; // sanity check
        final int initialCol = ItemBlockSack.getDyeColorForStack(sack);
        float r, g, b;
        int dyeCount;
        if (initialCol >= 0) {
            r = ((initialCol >>> 16) & 0xFF) / 255F;
            g = ((initialCol >>> 8) & 0xFF) / 255F;
            b = (initialCol & 0xFF) / 255F;
            dyeCount = 1;
        } else {
            r = 0F;
            g = 0F;
            b = 0F;
            dyeCount = 0;
        }
        for (int i = 0; i < size; i++) {
            if (i == sackSlot) continue;
            final Optional<EnumDyeColor> maybeCol = DyeUtils.colorFromStack(inv.getStackInSlot(i));
            if (maybeCol.isPresent()) {
                final float[] col = maybeCol.get().getColorComponentValues();
                r += col[0];
                g += col[1];
                b += col[2];
                dyeCount++;
            }
        }
        if (dyeCount == 0) return ItemStack.EMPTY; // sanity check
        final ItemStack dyedSack = sack.copy();
        dyedSack.setItemDamage(1);
        ItemBlockSack.setDyeColorForStack(dyedSack,
                (int) Math.floor(r * 255F / dyeCount) << 16
                        | (int) Math.floor(g * 255F / dyeCount) << 8
                        | (int) Math.floor(b * 255F / dyeCount));
        return dyedSack;
    }

    @Override
    public boolean canFit(final int width, final int height) {
        return width > 1 || height > 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

}
