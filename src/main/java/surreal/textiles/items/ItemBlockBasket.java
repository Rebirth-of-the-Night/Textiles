package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import surreal.textiles.entities.EntityBasket;
import surreal.textiles.util.ReadOnlyItemBlockInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public class ItemBlockBasket extends ItemBlockBase {

    public ItemBlockBasket(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new ReadOnlyItemBlockInventory(stack);
    }

    @Override
    public void registerModels() {
        for (int i = 0; i < 2; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=up,type=" + i));
        }
    }

    @Override
    public boolean hasCustomEntity(@Nonnull ItemStack stack) {
        return stack.hasTagCompound();
    }

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        return new EntityBasket(world, location, itemstack);
    }

}
