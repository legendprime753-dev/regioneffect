package de.regioneffect;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Optional;
import java.util.TreeSet;

public final class PotionEffectResolver {
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("strength", "increase_damage"),
            Map.entry("stärke", "increase_damage"),
            Map.entry("haste", "fast_digging"),
            Map.entry("eile", "fast_digging"),
            Map.entry("mining_fatigue", "slow_digging"),
            Map.entry("abbauverlangsamung", "slow_digging"),
            Map.entry("jump_boost", "jump"),
            Map.entry("sprungkraft", "jump"),
            Map.entry("instant_health", "heal"),
            Map.entry("heilung", "heal"),
            Map.entry("instant_damage", "harm"),
            Map.entry("schaden", "harm"),
            Map.entry("resistance", "damage_resistance"),
            Map.entry("resistenz", "damage_resistance"),
            Map.entry("night_vision", "night_vision"),
            Map.entry("nachtsicht", "night_vision"),
            Map.entry("water_breathing", "water_breathing"),
            Map.entry("wasseratmung", "water_breathing"),
            Map.entry("fire_resistance", "fire_resistance"),
            Map.entry("feuerresistenz", "fire_resistance"),
            Map.entry("weakness", "weakness"),
            Map.entry("schwäche", "weakness"),
            Map.entry("slowness", "slow"),
            Map.entry("langsamkeit", "slow"),
            Map.entry("speed", "speed"),
            Map.entry("schnelligkeit", "speed"),
            Map.entry("luck", "luck"),
            Map.entry("glück", "luck"),
            Map.entry("unluck", "unluck"),
            Map.entry("pech", "unluck"),
            Map.entry("saturation", "saturation"),
            Map.entry("sättigung", "saturation")
    );

    private PotionEffectResolver() {
    }

    public static Optional<PotionEffectType> resolve(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }

        String normalized = normalize(input);
        String aliasCandidate = ALIASES.get(normalized);
        if (aliasCandidate != null) {
            PotionEffectType aliased = findByKeyOrLegacyName(aliasCandidate);
            if (aliased != null) {
                return Optional.of(aliased);
            }
        }

        PotionEffectType direct = findByKeyOrLegacyName(normalized);
        return Optional.ofNullable(direct);
    }

    public static String toStorageKey(PotionEffectType type) {
        return type.getKey().getKey();
    }

    public static String displayName(PotionEffectType type) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String part : toStorageKey(type).split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            joiner.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
        }
        return joiner.toString();
    }

    public static Collection<String> suggestions() {
        TreeSet<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PotionEffectType type : Registry.EFFECT) {
            suggestions.add(type.getKey().getKey());
            String legacyName = type.getName();
            if (legacyName != null) {
                suggestions.add(legacyName.toLowerCase(Locale.ROOT));
            }
        }
        suggestions.addAll(ALIASES.keySet());
        return suggestions;
    }

    private static PotionEffectType findByKeyOrLegacyName(String candidate) {
        PotionEffectType byMinecraftKey = Registry.EFFECT.get(NamespacedKey.minecraft(candidate));
        if (byMinecraftKey != null) {
            return byMinecraftKey;
        }

        for (PotionEffectType type : Registry.EFFECT) {
            if (type.getKey().getKey().equalsIgnoreCase(candidate)) {
                return type;
            }

            String legacyName = type.getName();
            if (legacyName != null && normalize(legacyName).equals(candidate)) {
                return type;
            }
        }
        return null;
    }

    private static String normalize(String input) {
        return input.trim()
                .toLowerCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_')
                .replace(':', '_');
    }
}
