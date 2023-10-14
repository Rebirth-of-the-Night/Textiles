package surreal.textiles.items;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import surreal.textiles.client.models.ModelRegistry;

public class ItemFlaxSeeds extends ItemSeeds implements ModelRegistry {

    public ItemFlaxSeeds(Block crops) {
        super(crops, Blocks.AIR);
    }
}