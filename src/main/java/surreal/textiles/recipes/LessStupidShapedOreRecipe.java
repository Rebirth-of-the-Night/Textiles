package surreal.textiles.recipes;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class LessStupidShapedOreRecipe extends ShapedOreRecipe {

    public LessStupidShapedOreRecipe(ResourceLocation group, Block     result, Object... recipe){ this(group, new ItemStack(result), recipe); }
    public LessStupidShapedOreRecipe(ResourceLocation group, Item      result, Object... recipe){ this(group, new ItemStack(result), recipe); }
    public LessStupidShapedOreRecipe(ResourceLocation group, @Nonnull ItemStack result, Object... recipe) { this(group, result, CraftingHelper.parseShaped(recipe)); }
    public LessStupidShapedOreRecipe(ResourceLocation group, @Nonnull ItemStack result, CraftingHelper.ShapedPrimer primer) {
        super(group, result, primer);
        setRegistryName(group);
    }
}
