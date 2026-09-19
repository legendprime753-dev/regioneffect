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
    public static final long REFRESH_INTERVAL_TICKS = 40L;

    private static final int EFFECT_DURATION_TICKS = 100;
    private static final int REAPPLY_THRESHOLD_TICKS = 40;

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
            if (shouldApply(player, entry.getKey(), entry.getValue())) {
                apply(player, entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<PotionEffectType, Integer> entry : previous.entrySet()) {
            if (!next.containsKey(entry.getKey())) {
                clearManagedEffect(player, entry.getKey(), entry.getValue());
            }
        }

        if (next.isEmpty()) {
            activeEffectsByPlayer.remove(player.getUniqueId());
        } else {
            activeEffectsByPlayer.put(player.getUniqueId(), Map.copyOf(next));
        }
    }

    public void clearPlayer(Player player) {
        Map<PotionEffectType, Integer> tracked = activeEffectsByPlayer.remove(player.getUniqueId());
        if (tracked == null || tracked.isEmpty()) {
            return;
        }

        for (Map.Entry<PotionEffectType, Integer> entry : tracked.entrySet()) {
            clearManagedEffect(player, entry.getKey(), entry.getValue());
        }
    }

    public void clearAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            clearPlayer(player);
        }
    }

    public void refreshAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            updatePlayer(player, player.getLocation());
        }
    }

    private boolean shouldApply(Player player, PotionEffectType type, int amplifier) {
        PotionEffect current = player.getPotionEffect(type);
        if (current == null) {
            return true;
        }

        if (current.getAmplifier() < amplifier) {
            return true;
        }

        if (isManagedEffect(current, current.getAmplifier())) {
            return current.getAmplifier() != amplifier || current.getDuration() <= REAPPLY_THRESHOLD_TICKS;
        }

        return false;
    }

    private void clearManagedEffect(Player player, PotionEffectType type, int amplifier) {
        PotionEffect current = player.getPotionEffect(type);
        if (current != null && isManagedEffect(current, amplifier)) {
            player.removePotionEffect(type);
        }
    }

    private void apply(Player player, PotionEffectType type, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, EFFECT_DURATION_TICKS, amplifier, true, false, false));
    }

    private boolean isManagedEffect(PotionEffect effect, int amplifier) {
        return effect.getAmplifier() == amplifier
                && effect.isAmbient()
                && !effect.hasParticles()
                && !effect.hasIcon()
                && effect.getDuration() <= EFFECT_DURATION_TICKS;
    }
}
