package de.regioneffect;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PotionEffectResolver {
    private static final Map<String, String> ALIASES = createAliases();

    private PotionEffectResolver() {
    }

    public static Optional<PotionEffectType> resolve(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(input);
        String candidate = ALIASES.getOrDefault(normalized, normalized);

        PotionEffectType byKey = Registry.EFFECT.get(NamespacedKey.minecraft(candidate));
        if (byKey != null) {
            return Optional.of(byKey);
        }

        for (PotionEffectType type : Registry.EFFECT) {
            String key = type.getKey().getKey();
            if (key.equalsIgnoreCase(candidate) || normalize(key).equals(candidate)) {
                return Optional.of(type);
            }

            String legacyName = type.getName();
            if (legacyName != null && normalize(legacyName).equals(candidate)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

    public static String toStorageKey(PotionEffectType type) {
        return type.getKey().getKey();
    }

    private static String normalize(String input) {
        return input.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_')
                .replace(':', '_');
    }

    private static Map<String, String> createAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("strength", "strength");
        aliases.put("stärke", "strength");
        aliases.put("speed", "speed");
        aliases.put("schnelligkeit", "speed");
        aliases.put("slowness", "slowness");
        aliases.put("langsamkeit", "slowness");
        aliases.put("haste", "haste");
        aliases.put("eile", "haste");
        aliases.put("mining_fatigue", "mining_fatigue");
        aliases.put("abbauverlangsamung", "mining_fatigue");
        aliases.put("jump_boost", "jump_boost");
        aliases.put("sprungkraft", "jump_boost");
        aliases.put("resistance", "resistance");
        aliases.put("resistenz", "resistance");
        aliases.put("instant_health", "instant_health");
        aliases.put("heal", "instant_health");
        aliases.put("heilung", "instant_health");
        aliases.put("instant_damage", "instant_damage");
        aliases.put("harm", "instant_damage");
        aliases.put("schaden", "instant_damage");
        aliases.put("regeneration", "regeneration");
        aliases.put("sättigung", "saturation");
        aliases.put("saturation", "saturation");
        aliases.put("luck", "luck");
        aliases.put("glück", "luck");
        aliases.put("unluck", "unluck");
        aliases.put("pech", "unluck");
        return Map.copyOf(aliases);
    }
}
