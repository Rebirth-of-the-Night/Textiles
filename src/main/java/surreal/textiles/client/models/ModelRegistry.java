package surreal.textiles.client.models;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public interface ModelRegistry {
    @SideOnly(Side.CLIENT)
    default void registerModels() {
        ModelLoader.setCustomModelResourceLocation(getItem(), 0, getModelLocation());
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(Objects.requireNonNull(getItem().getRegistryName()), "inventory");
    }

    default Item getItem() {
        return (Item) this;
    }
}
