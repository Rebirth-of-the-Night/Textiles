package surreal.textiles;

import net.minecraftforge.common.config.Config;

@Config(modid = Textiles.MODID)
public class ModConfig {

    public static final Drops drops = new Drops();
    public static final FeatherBundle featherBundle = new FeatherBundle();
    public static final Cushions cushions = new Cushions();
    public static final FiberBales fibers = new FiberBales();
    public static final Fabric fabric = new Fabric();
    public static final Sack sack = new Sack();
    public static final Basket basket = new Basket();
    public static final Overencumbrance overencumbrance = new Overencumbrance();

    public static class Drops {
        @Config.Name("Plant Fibers Drop")
        @Config.Comment("Chance of Raw Plant Fibers drop when player breaks Tall Grass.")
        @Config.RangeDouble(min = 0)
        public double plantFibersDrop = 0.2;

        @Config.Name("Silk Wisps Drop")
        @Config.Comment("Replace Cobwebs drop with Silk Wisps")
        public boolean replaceCobwebDrop = false;

        @Config.Name("Always Drop Seeds")
        @Config.Comment("If Flax Crop should always drop seed no matter it's age.")
        public boolean alwaysDropSeeds = false;
    }

    public static class Cushions {
        @Config.Name("Fall Damage Reduction")
        @Config.Comment({
                "The flat amount of effective fall distance reduction applied by landing on a bundled feather block.",
                "The formula used is (EffectiveDistance) = (RealDistance) * (1.0 - (Reduction))",
                "Therefore, a value of 0.3 will reduce the effective fall distance by 30%."
        })
        @Config.RangeDouble(min = 0D)
        public double damageReduction = 0.4D;

        @Config.Name("Walking Speed")
        @Config.Comment({
                "Walking speed multiplication when player walks on it",
                "Works like soul sand"
        })
        @Config.RangeDouble(min = 0D)
        public double walkingSpeed = 0.8D;
    }

    public static class FiberBales {
        @Config.Name("Update Chance")
        @Config.Comment("Chance of Raw Fibers updating itself to slowly turn into Retted Fibers")
        @Config.RangeDouble(min = 0)
        public double updateChance = 0.1D;

        @Config.Name("Update Delay")
        @Config.Comment("Update delay in seconds")
        @Config.RangeInt(min = 1)
        public int updateDelay = 15;

        @Config.Name("Max Twine Drop")
        @Config.Comment("Max amount of Twine drop from Retted Fibers")
        @Config.RangeInt(min = 0)
        public int twineDrop = 2;

        @Config.Name("Furnace Fuel Amount")
        @Config.Comment("Furnace Fuel Amount for Fiber Bales")
        public int fuelAmount = 200;
    }

    public static class Fabric {
        @Config.Name("Furnace Fuel Amount")
        @Config.Comment("Furnace Fuel Amount for Fabrics")
        public int fuelAmount = 50;
    }

    public static class FeatherBundle {
        @Config.Name("Fall Damage Reduction")
        @Config.Comment({
                "The flat amount of effective fall distance reduction applied by landing on a bundled feather block.",
                "The formula used is (EffectiveDistance) = (RealDistance) * (1.0 - (Reduction))",
                "Therefore, a value of 0.3 will reduce the effective fall distance by 30%."
        })
        @Config.RangeDouble(min = 0D)
        public double damageReduction = 0.2D;

        @Config.Name("Break Treshold")
        @Config.Comment("The fall-distance threshold, in blocks, beyond which falling upon a feather bundle block will cause it to break.")
        @Config.RangeDouble(min = 0D)
        public double breakTreshold = 0.8D;

        @Config.Name("Walking Speed")
        @Config.Comment({
                "Walking speed multiplication when player walks on it",
                "Works like soul sand"
        })
        @Config.RangeDouble(min = 0D)
        public double walkingSpeed = 0.6D;
    }

    public static class Sack {
        @Config.Name("Slot Row Count")
        @Config.Comment({
                "How many rows of slots should the sack have?",
                "Reducing this may cause existing sacks to delete items!"
        })
        @Config.RangeInt(min = 1, max = 3)
        @Config.RequiresWorldRestart
        public int slotRowCount = 3;

        @Config.Name("Slot Column Count")
        @Config.Comment({
                "How many columns of slots should the sack have?",
                "Reducing this may cause existing sacks to delete items!"
        })
        @Config.RangeInt(min = 1, max = 9)
        @Config.RequiresWorldRestart
        public int slotColumnCount = 3;

        @Config.Name("Inventory Interaction")
        @Config.Comment("Allows bundle-like interaction with the sack in the inventory.")
        public boolean inventoryInteraction = true;

        @Config.Name("Gravity")
        @Config.Comment("Should sacks be falling blocks (like sand)?")
        public boolean gravity = true;

        @Config.Name("Gravity Damage")
        @Config.Comment("The amount of damage dealt, per non-empty inventory slot, per block fallen, by falling sacks.")
        @Config.RangeDouble(min = 0D)
        public double gravityDamage = 0.2D;
    }

    public static class Basket {
        @Config.Name("Keep Inventory")
        @Config.Comment("Should the (non-sturdy) basket retain its inventory when broken?")
        @Config.RequiresWorldRestart
        public boolean keepInventory = true;

        @Config.Name("Keep Inventory (Sturdy)")
        @Config.Comment("Should the sturdy basket retain its inventory when broken?")
        @Config.RequiresWorldRestart
        public boolean keepInventorySturdy = true;

        @Config.Name("Inventory Interaction")
        @Config.Comment({
                "Allows bundle-like interaction with the basket in the inventory.",
                "Only useful when the \"keep inventory\" setting is enabled."
        })
        public boolean inventoryInteraction = true;
    }

    public static class Overencumbrance {
        @Config.Name("Enable Overencumbrance")
        @Config.Comment("Enables a movement-slowing effect when the player is holding too many \"heavy\" items.")
        public boolean enabled = true;

        @Config.Name("Weight Threshold")
        @Config.Comment("The minimum weight at which overencumbrance applies.")
        @Config.RangeDouble(min = 0D)
        public double weightThreshold = 3D;

        @Config.Name("Item Weights")
        @Config.Comment({
                "A list of custom item weights.",
                "Each line should be of the form: <item_id>[:<metadata>]=<weight>"
        })
        @Config.RequiresMcRestart
        public String[] itemWeights = {
                "textiles:sack=1",
                "textiles:basket=2"
        };

        @Config.Name("Search Inside Inventories")
        @Config.Comment({
                "Should the overencumbrance check include the contexts of items with inventories, like shulker boxes?",
                "If the inventory item itself has a weight, then it gets added to the weight of its contents."
        })
        public boolean searchInsideInventories = true;

        @Config.Name("Inventories Light When Empty")
        @Config.Comment("Should items with inventories only be considered heavy if they contain at least one item?")
        public boolean inventoriesLightWhenEmpty = true;

        @Config.Name("Overencumbrance Effect")
        @Config.Comment("The potion effect applied by overencumbrance.")
        @Config.RequiresMcRestart
        public String overencumbranceEffect = "minecraft:slowness";

        @Config.Name("Overencumbrance Effect Potency")
        @Config.Comment("The level of the overencumbrance potion effect applied per unit of weight over the limit.")
        @Config.RangeDouble(min = 0D)
        public double overencumbranceEffectPotency = 1D;

        @Config.Name("Strength Effects")
        @Config.Comment({
                "A list of potion effects that increase a player's carry capacity by a certain amount per level.",
                "Each line should be of the form: <potion_id>=<weight_per_level>"
        })
        @Config.RequiresMcRestart
        public String[] strengthEffects = {
                "minecraft:strength=1"
        };
    }
}
