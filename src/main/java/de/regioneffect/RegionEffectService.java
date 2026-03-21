package de.regioneffect;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionEffectService {
    private static final int EFFECT_DURATION_TICKS = 60;

    private final RegionManager regionManager;
    private final Map<UUID, Map<PotionEffectType, Integer>> activeEffectsByPlayer = new ConcurrentHashMap<>();

    public RegionEffectService(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    public void updatePlayer(Player player, Location location) {
        List<CuboidRegion> currentRegions = regionManager.getRegionsAt(location);
        Map<PotionEffectType, Integer> next = new HashMap<>();
        for (CuboidRegion region : currentRegions) {
            RegionEffect effect = region.getEffect();
            if (effect == null) {
                continue;
            }

            next.merge(effect.type(), effect.amplifier(), Math::max);
        }

        Map<PotionEffectType, Integer> previous = activeEffectsByPlayer.getOrDefault(player.getUniqueId(), Map.of());

        for (Map.Entry<PotionEffectType, Integer> entry : next.entrySet()) {
            Integer previousAmplifier = previous.get(entry.getKey());
            if (previousAmplifier == null || previousAmplifier != entry.getValue()) {
                apply(player, entry.getKey(), entry.getValue());
            }
        }

        for (PotionEffectType previousType : previous.keySet()) {
            if (!next.containsKey(previousType)) {
                player.removePotionEffect(previousType);
            }
        }

        if (next.isEmpty()) {
            activeEffectsByPlayer.remove(player.getUniqueId());
        } else {
            activeEffectsByPlayer.put(player.getUniqueId(), Map.copyOf(next));
        }
    }

    public void clearTracking(UUID playerId) {
        activeEffectsByPlayer.remove(playerId);
    }

    public void refreshAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            updatePlayer(player, player.getLocation());
        }
    }

    private void apply(Player player, PotionEffectType type, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, EFFECT_DURATION_TICKS, amplifier, true, false, false));
    }
}
