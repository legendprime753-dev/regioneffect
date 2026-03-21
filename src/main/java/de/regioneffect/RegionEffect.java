package de.regioneffect;

import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public record RegionEffect(PotionEffectType type, int amplifier) {
    public RegionEffect {
        Objects.requireNonNull(type, "type");
        if (amplifier < 0) {
            throw new IllegalArgumentException("amplifier must be >= 0");
        }
    }
}
