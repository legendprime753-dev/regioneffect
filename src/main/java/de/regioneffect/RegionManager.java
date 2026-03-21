package de.regioneffect;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class RegionManager {
    private final RegioneffectPlugin plugin;
    private final File file;
    private final Map<String, CuboidRegion> regionsByName;
    private final Map<String, List<CuboidRegion>> regionsByWorld;

    public RegionManager(RegioneffectPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "regions.yml");
        this.regionsByName = new ConcurrentHashMap<>();
        this.regionsByWorld = new ConcurrentHashMap<>();
    }

    public synchronized void load() {
        ensureFileExists();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, CuboidRegion> loaded = new ConcurrentHashMap<>();
        Map<String, List<CuboidRegion>> byWorld = new ConcurrentHashMap<>();

        ConfigurationSection section = config.getConfigurationSection("regions");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                ConfigurationSection regionSection = section.getConfigurationSection(name);
                if (regionSection == null) {
                    continue;
                }

                String world = regionSection.getString("world");
                if (world == null || world.isBlank()) {
                    plugin.getLogger().warning("Skipping region '" + name + "' because world is missing.");
                    continue;
                }

                RegionEffect effect = null;
                String effectName = regionSection.getString("effect.type");
                if (effectName != null && !effectName.isBlank()) {
                    PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase(Locale.ROOT));
                    if (type != null) {
                        effect = new RegionEffect(type, Math.max(0, regionSection.getInt("effect.amplifier", 0)));
                    } else {
                        plugin.getLogger().warning("Skipping invalid effect '" + effectName + "' for region '" + name + "'.");
                    }
                }

                CuboidRegion region = new CuboidRegion(
                        name,
                        world,
                        regionSection.getInt("min.x"),
                        regionSection.getInt("min.y"),
                        regionSection.getInt("min.z"),
                        regionSection.getInt("max.x"),
                        regionSection.getInt("max.y"),
                        regionSection.getInt("max.z"),
                        effect
                );
                loaded.put(normalize(name), region);
                byWorld.computeIfAbsent(world, ignored -> new ArrayList<>()).add(region);
            }
        }

        byWorld.replaceAll((ignored, list) -> List.copyOf(list));
        regionsByName.clear();
        regionsByName.putAll(loaded);
        regionsByWorld.clear();
        regionsByWorld.putAll(byWorld);
    }

    public synchronized void save() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection section = config.createSection("regions");
        for (CuboidRegion region : regionsByName.values()) {
            ConfigurationSection regionSection = section.createSection(region.getName());
            regionSection.set("world", region.getWorldName());
            regionSection.set("min.x", region.getMinX());
            regionSection.set("min.y", region.getMinY());
            regionSection.set("min.z", region.getMinZ());
            regionSection.set("max.x", region.getMaxX());
            regionSection.set("max.y", region.getMaxY());
            regionSection.set("max.z", region.getMaxZ());
            RegionEffect effect = region.getEffect();
            if (effect != null) {
                regionSection.set("effect.type", effect.type().getName());
                regionSection.set("effect.amplifier", effect.amplifier());
            }
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not save regions.yml", exception);
        }
    }

    public synchronized Optional<CuboidRegion> createRegion(String name, Location pos1, Location pos2) {
        String key = normalize(name);
        if (regionsByName.containsKey(key)) {
            return Optional.empty();
        }

        CuboidRegion region = new CuboidRegion(
                name,
                pos1.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                null
        );
        regionsByName.put(key, region);
        rebuildWorldIndex(region.getWorldName());
        save();
        return Optional.of(region);
    }

    public synchronized boolean setEffect(String regionName, RegionEffect effect) {
        CuboidRegion region = regionsByName.get(normalize(regionName));
        if (region == null) {
            return false;
        }

        region.setEffect(effect);
        save();
        return true;
    }

    public Optional<CuboidRegion> findRegion(String name) {
        return Optional.ofNullable(regionsByName.get(normalize(name)));
    }

    public Collection<CuboidRegion> getRegionsForWorld(String worldName) {
        return regionsByWorld.getOrDefault(worldName, Collections.emptyList());
    }

    public Collection<CuboidRegion> getAllRegions() {
        return Collections.unmodifiableCollection(new LinkedHashSet<>(regionsByName.values()));
    }

    public List<CuboidRegion> getRegionsAt(Location location) {
        if (location.getWorld() == null) {
            return Collections.emptyList();
        }

        List<CuboidRegion> matches = new ArrayList<>();
        for (CuboidRegion region : getRegionsForWorld(location.getWorld().getName())) {
            if (region.contains(location)) {
                matches.add(region);
            }
        }
        return matches;
    }

    private void ensureFileExists() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }
        if (!file.exists()) {
            plugin.saveResource("regions.yml", false);
        }
    }

    private void rebuildWorldIndex(String worldName) {
        List<CuboidRegion> regions = regionsByName.values().stream()
                .filter(region -> region.getWorldName().equals(worldName))
                .toList();
        regionsByWorld.put(worldName, List.copyOf(regions));
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
