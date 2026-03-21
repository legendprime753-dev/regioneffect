package de.regioneffect;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RegioneffectCommand implements CommandExecutor, TabCompleter {
    private final RegioneffectPlugin plugin;

    public RegioneffectCommand(RegioneffectPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(RegioneffectPlugin.ADMIN_PERMISSION)) {
            sender.sendMessage("§cDafür hast du keine Berechtigung.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eVerwendung: /regioneffect <create|effect|delete|reload>");
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> handleCreate(sender, args);
            case "effect" -> handleEffect(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage("§cUnbekannter Unterbefehl.");
                yield true;
            }
        };
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur Spieler können Regionen mit einer Auswahl erstellen.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§eVerwendung: /regioneffect create <Name>");
            return true;
        }

        Selection selection = plugin.getSelection(player.getUniqueId());
        if (selection == null || !selection.isComplete()) {
            sender.sendMessage("§cDu musst zuerst Position 1 und Position 2 mit der Mace setzen.");
            return true;
        }
        if (!selection.isSameWorld()) {
            sender.sendMessage("§cBeide Positionen müssen in derselben Welt liegen.");
            return true;
        }

        Location pos1 = selection.getPos1();
        Location pos2 = selection.getPos2();
        boolean created = plugin.getRegionManager().createRegion(args[1], pos1, pos2).isPresent();
        if (!created) {
            sender.sendMessage("§cEine Region mit diesem Namen existiert bereits.");
            return true;
        }

        sender.sendMessage("§aRegion '§f" + args[1] + "§a' wurde gespeichert.");
        plugin.getEffectService().refreshAll(plugin.getServer().getOnlinePlayers());
        return true;
    }

    private boolean handleEffect(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§eVerwendung: /regioneffect effect <Region> <Effect> <Stärke>");
            return true;
        }

        PotionEffectType type = PotionEffectResolver.resolve(args[2]).orElse(null);
        if (type == null) {
            sender.sendMessage("§cUnbekannter Potion-Effekt: " + args[2]);
            return true;
        }

        int amplifier;
        try {
            amplifier = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            sender.sendMessage("§cDie Stärke muss eine Zahl >= 0 sein.");
            return true;
        }

        if (amplifier < 0) {
            sender.sendMessage("§cDie Stärke muss eine Zahl >= 0 sein.");
            return true;
        }

        boolean success = plugin.getRegionManager().setEffect(args[1], new RegionEffect(type, amplifier));
        if (!success) {
            sender.sendMessage("§cDie Region '" + args[1] + "' wurde nicht gefunden.");
            return true;
        }

        sender.sendMessage("§aEffekt für Region '§f" + args[1] + "§a' gesetzt: §f" + PotionEffectResolver.toStorageKey(type) + " §7(Stärke " + amplifier + ")");
        plugin.getEffectService().refreshAll(plugin.getServer().getOnlinePlayers());
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§eVerwendung: /regioneffect delete <Region>");
            return true;
        }

        boolean success = plugin.getRegionManager().deleteRegion(args[1]);
        if (!success) {
            sender.sendMessage("§cDie Region '" + args[1] + "' wurde nicht gefunden.");
            return true;
        }

        plugin.getEffectService().refreshAll(plugin.getServer().getOnlinePlayers());
        sender.sendMessage("§aRegion '§f" + args[1] + "§a' wurde gelöscht.");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getRegionManager().load();
        plugin.getEffectService().refreshAll(plugin.getServer().getOnlinePlayers());
        sender.sendMessage("§aRegioneffect wurde neu geladen.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(RegioneffectPlugin.ADMIN_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return filter(List.of("create", "effect", "delete", "reload"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("effect") || args[0].equalsIgnoreCase("delete"))) {
            return filter(plugin.getRegionManager().getAllRegions().stream()
                    .map(CuboidRegion::getName)
                    .toList(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("effect")) {
            return filter(new ArrayList<>(PotionEffectResolver.suggestions()), args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("effect")) {
            return List.of("0", "1", "2", "3");
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option != null && option.toLowerCase(Locale.ROOT).startsWith(lower))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
