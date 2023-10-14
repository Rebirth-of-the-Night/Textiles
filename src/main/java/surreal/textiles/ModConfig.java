package surreal.textiles;

import net.minecraftforge.common.config.Config;

@Config(modid = Textiles.MODID)
public class ModConfig {

    public static final Drops drops = new Drops();
    public static final FeatherBundle featherBundle = new FeatherBundle();
    public static final Cushions cushions = new Cushions();
    public static final FiberBales fibers = new FiberBales();
    public static final Fabric fabric = new Fabric();

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
}
