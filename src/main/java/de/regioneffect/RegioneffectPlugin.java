package de.regioneffect;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RegioneffectPlugin extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "regioneffect.admin";

    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    private RegionManager regionManager;
    private RegionEffectService effectService;

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
        getServer().getScheduler().runTaskTimer(this, () -> effectService.refreshAll(getServer().getOnlinePlayers()), 40L, 40L);
        effectService.refreshAll(getServer().getOnlinePlayers());
    }

    @Override
    public void onDisable() {
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
