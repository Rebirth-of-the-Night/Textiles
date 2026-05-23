package surreal.textiles.event;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import surreal.textiles.ModConfig;
import surreal.textiles.Textiles;
import surreal.textiles.util.TextilesUtils;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Mod.EventBusSubscriber(modid = Textiles.MODID)
public enum OverencumbranceHandler {

    ;

    private static final int CHECK_INTERVAL = 50;
    private static final int EFFECT_DURATION = CHECK_INTERVAL + 8;

    private static ConfigState configState = null;

    public static void loadConfig() {
        if (configState != null) throw new IllegalStateException("Item weights already loaded!");

        final Map<Item, WeightEntry> itemWeights = new IdentityHashMap<>(); // registry objects should be unique
        for (final String weightString : ModConfig.overencumbrance.itemWeights) {
            final WeightEntry entry;
            try {
                entry = parseWeightString(weightString);
            } catch (IllegalArgumentException e) {
                Textiles.LOGGER.warn("Ignoring invalid item weight in configuration: {}", weightString);
                Textiles.LOGGER.warn("  ({})", e.getMessage());
                continue;
            }
            itemWeights.put(entry.item, entry);
        }

        final Map<Potion, StrengthEntry> strengthEffects = new IdentityHashMap<>();
        for (final String effectString : ModConfig.overencumbrance.strengthEffects) {
            final StrengthEntry entry;
            try {
                entry = parseStrengthEffectString(effectString);
            } catch (IllegalArgumentException e) {
                Textiles.LOGGER.warn("Ignoring invalid strength effect in configuration: {}", effectString);
                Textiles.LOGGER.warn("  ({})", e.getMessage());
                continue;
            }
            strengthEffects.put(entry.potion, entry);
        }

        final ResourceLocation potionId = new ResourceLocation(ModConfig.overencumbrance.overencumbranceEffect);
        final Potion overencumberEffect = ForgeRegistries.POTIONS.getValue(potionId);
        if (overencumberEffect == null) {
            throw new NoSuchElementException("Unknown potion effect configured for overencumbrance: " + potionId);
        }

        configState = new ConfigState(itemWeights, strengthEffects, overencumberEffect);
    }

    private static WeightEntry parseWeightString(final String weightString) {
        final int eqIndex = weightString.indexOf('=');
        if (eqIndex < 0) throw new IllegalArgumentException("Missing '='");
        final String[] itemParts = weightString.substring(0, eqIndex).split(":", 3);
        final ResourceLocation itemId;
        final int meta;
        switch (itemParts.length) {
            case 1: // just an item name
                itemId = new ResourceLocation(itemParts[0]);
                meta = OreDictionary.WILDCARD_VALUE;
                break;
            case 2: // domain and item name
                itemId = new ResourceLocation(itemParts[0], itemParts[1]);
                meta = OreDictionary.WILDCARD_VALUE;
                break;
            case 3: // domain, item name, and meta
                itemId = new ResourceLocation(itemParts[0], itemParts[1]);
                try {
                    meta = Integer.parseInt(itemParts[2], 10);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid item meta: " + itemParts[2]);
                }
                break;
            default:
                throw new IllegalStateException();
        }
        final Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) throw new IllegalArgumentException("No such item: " + itemId);
        final String valueString = weightString.substring(eqIndex + 1);
        final float weight = Float.parseFloat(valueString);
        if (!Float.isFinite(weight)) throw new IllegalArgumentException("Invalid weight value: " + valueString);
        return new WeightEntry(item, meta, weight);
    }

    private static StrengthEntry parseStrengthEffectString(final String effectString) {
        final int eqIndex = effectString.indexOf('=');
        if (eqIndex < 0) throw new IllegalArgumentException("Missing '='");
        final String[] effectParts = effectString.substring(0, eqIndex).split(":", 2);
        final ResourceLocation potionId = switch (effectParts.length) {
            case 1 -> new ResourceLocation(effectParts[0]);
            case 2 -> new ResourceLocation(effectParts[0], effectParts[1]);
            default -> throw new IllegalStateException();
        };
        final Potion potion = ForgeRegistries.POTIONS.getValue(potionId);
        if (potion == null) throw new IllegalArgumentException("No such potion effect: " + potionId);
        final String valueString = effectString.substring(eqIndex + 1);
        final float weight = Float.parseFloat(valueString);
        if (!Float.isFinite(weight)) throw new IllegalArgumentException("Invalid weight value: " + valueString);
        return new StrengthEntry(potion, weight);
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.ServerTickEvent event) {
        if (!ModConfig.overencumbrance.enabled || event.side != Side.SERVER) return;
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.getTickCounter() % CHECK_INTERVAL != 0) return;
        for (final EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            final IItemHandler inventory = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inventory == null) continue; // sanity check

            final World world = player.world;
            float weight = 0;
            final int slotCount = inventory.getSlots();
            for (int i = 0; i < slotCount; i++) {
                weight += getStackWeight(world, inventory.getStackInSlot(i));
            }

            final Collection<PotionEffect> activeEffects = player.getActivePotionEffects();
            final Map<Potion, StrengthEntry> strengthEffects = configState.strengthEffects;
            if (activeEffects.size() <= strengthEffects.size()) { // a bit scuffed
                for (final PotionEffect effect : activeEffects) {
                    final StrengthEntry entry = strengthEffects.get(effect.getPotion());
                    if (entry == null) continue;
                    weight -= entry.weight * (effect.getAmplifier() + 1);
                }
            } else {
                for (final Map.Entry<Potion, StrengthEntry> pair : strengthEffects.entrySet()) {
                    final StrengthEntry entry = pair.getValue();
                    final PotionEffect effect = player.getActivePotionEffect(entry.potion);
                    if (effect == null) continue;
                    weight -= entry.weight * (effect.getAmplifier() + 1);
                }
            }

            final float overWeight = weight - (float) ModConfig.overencumbrance.weightThreshold;
            if (overWeight < 0) continue;
            final int potency = MathHelper.clamp(
                    (int) Math.floor(overWeight / ModConfig.overencumbrance.overencumbranceEffectPotency), 0, 255);
            final Potion potion = configState.overencumberEffect;
            final PotionEffect activeEffect = player.getActivePotionEffect(potion);
            if (activeEffect != null && potency < activeEffect.getAmplifier()) { // override effect when stepping down
                if (activeEffect.getDuration() >= EFFECT_DURATION) return;
                player.removePotionEffect(potion);
            }
            player.addPotionEffect(new PotionEffect(potion, EFFECT_DURATION, potency, false, false));
        }
    }

    private static float getStackWeight(final World world, final ItemStack stack) {
        final WeightEntry provider = configState.itemWeights.get(stack.getItem());

        if (ModConfig.overencumbrance.searchInsideInventories) {
            final IItemHandler inventory = TextilesUtils.getItemInventoryForRead(world, stack);
            if (inventory == null) {
                return provider != null ? provider.getStackWeight(stack) : 0F;
            }
            float weight = 0;
            boolean nonEmpty = false;
            final int slotCount = inventory.getSlots();
            for (int i = 0; i < slotCount; i++) {
                final ItemStack slotStack = inventory.getStackInSlot(i);
                if (slotStack.isEmpty()) continue;
                nonEmpty = true;
                weight += getStackWeight(world, slotStack);
            }
            if ((nonEmpty || !ModConfig.overencumbrance.inventoriesLightWhenEmpty) && provider != null) {
                weight += provider.getStackWeight(stack);
            }
            return weight;
        }

        if (provider == null) return 0F;
        final float weight = provider.getStackWeight(stack);
        if (weight == 0F) return 0F;

        if (!ModConfig.overencumbrance.inventoriesLightWhenEmpty) return weight;
        final IItemHandler inventory = TextilesUtils.getItemInventoryForRead(world, stack);
        if (inventory == null) return weight;

        final int slotCount = inventory.getSlots();
        for (int i = 0; i < slotCount; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return weight;
            }
        }
        return 0F;
    }

    @Desugar
    private record WeightEntry(Item item, int meta, float weight) {

        float getStackWeight(final ItemStack stack) {
            return meta == OreDictionary.WILDCARD_VALUE || meta == stack.getMetadata() ? weight * stack.getCount() : 0F;
        }

    }

    @Desugar
    private record StrengthEntry(Potion potion, float weight) {}

    @Desugar
    private record ConfigState(Map<Item, WeightEntry> itemWeights, Map<Potion, StrengthEntry> strengthEffects,
                               Potion overencumberEffect) {}

}
