package de.regioneffect;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public final class CuboidRegion {
    private final String name;
    private final String worldName;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private volatile RegionEffect effect;

    public CuboidRegion(String name, String worldName, int x1, int y1, int z1, int x2, int y2, int z2, RegionEffect effect) {
        this.name = Objects.requireNonNull(name, "name");
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public RegionEffect getEffect() {
        return effect;
    }

    public void setEffect(RegionEffect effect) {
        this.effect = effect;
    }

    public boolean contains(Location location) {
        if (location == null) {
            return false;
        }

        World world = location.getWorld();
        if (world == null || !world.getName().equals(worldName)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }
}
