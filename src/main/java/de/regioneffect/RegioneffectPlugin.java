package de.regioneffect;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegioneffectPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "regioneffect.admin";

    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    private RegionManager regionManager;
    private RegionEffectService effectService;
    private BukkitTask effectRefreshTask;

    @Override
    public void onEnable() {
        this.regionManager = new RegionManager(this);
        this.regionManager.load();
        this.effectService = new RegionEffectService(regionManager);

        RegioneffectCommand commandExecutor = new RegioneffectCommand(this);
        PluginCommand command = getCommand("regioneffect");
        if (command == null) {
            throw new IllegalStateException("Command 'regioneffect' is missing in plugin.yml");
        }
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
        getServer().getPluginManager().registerEvents(new RegionEffectListener(this), this);
        effectService.refreshAll(getServer().getOnlinePlayers());
        effectRefreshTask = getServer().getScheduler().runTaskTimer(
                this,
                () -> effectService.refreshAll(getServer().getOnlinePlayers()),
                RegionEffectService.REFRESH_INTERVAL_TICKS,
                RegionEffectService.REFRESH_INTERVAL_TICKS
        );
    }

    @Override
    public void onDisable() {
        if (effectRefreshTask != null) {
            effectRefreshTask.cancel();
            effectRefreshTask = null;
        }
        if (effectService != null) {
            effectService.clearAll(getServer().getOnlinePlayers());
        }
        if (regionManager != null) {
            regionManager.save();
        }
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public RegionEffectService getEffectService() {
        return effectService;
    }

    public Selection getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public Selection getOrCreateSelection(UUID playerId) {
        return selections.computeIfAbsent(playerId, ignored -> new Selection());
    }

    public void removeSelection(UUID playerId) {
        selections.remove(playerId);
    }
}
