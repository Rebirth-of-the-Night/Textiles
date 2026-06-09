package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import surreal.textiles.ModConfig;
import surreal.textiles.blocks.BlockBasket;
import surreal.textiles.tiles.TileBasket;
import surreal.textiles.util.BlockItemInventory;

import javax.annotation.Nullable;
import java.util.Objects;

public class ItemBlockBasket extends ItemBlockBase implements PortableInventoryItem {

    public ItemBlockBasket(Block block) {
        super(block);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    @Nullable
    @Override
    public BlockItemInventory getPortableInventory(final ItemStack stack) {
        return ModConfig.basket.inventoryInteraction ? TileBasket.wrapStackInventory(stack, false) : null;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return TileBasket.wrapStackInventory(stack, true);
    }

    @Override
    public void registerModels() {
        final ResourceLocation regName = Objects.requireNonNull(getRegistryName());
        for (final BlockBasket.Type type : BlockBasket.Type.VALUES) {
            ModelLoader.setCustomModelResourceLocation(this, type.ordinal(), new ModelResourceLocation(
                    regName, "facing=up,type=" + type.getName()));
        }
    }

}
