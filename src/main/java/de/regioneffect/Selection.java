package de.regioneffect;

import org.bukkit.Location;

public final class Selection {
    private Location pos1;
    private Location pos2;

    public Location getPos1() {
        return pos1 == null ? null : pos1.clone();
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1 == null ? null : pos1.toBlockLocation();
    }

    public Location getPos2() {
        return pos2 == null ? null : pos2.clone();
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2 == null ? null : pos2.toBlockLocation();
    }

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }

    public boolean isSameWorld() {
        return isComplete() && pos1.getWorld() != null && pos2.getWorld() != null
                && pos1.getWorld().getUID().equals(pos2.getWorld().getUID());
    }
}
