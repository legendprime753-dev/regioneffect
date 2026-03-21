package de.regioneffect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class SelectionListener implements Listener {
    private final RegioneffectPlugin plugin;

    public SelectionListener(RegioneffectPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getMaterial() != Material.MACE) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission(RegioneffectPlugin.ADMIN_PERMISSION)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Selection selection = plugin.getOrCreateSelection(player.getUniqueId());
        if (action == Action.LEFT_CLICK_BLOCK) {
            selection.setPos1(clickedBlock.getLocation());
            player.sendMessage("§aPosition 1 gesetzt: §f" + format(clickedBlock));
        } else {
            selection.setPos2(clickedBlock.getLocation());
            player.sendMessage("§aPosition 2 gesetzt: §f" + format(clickedBlock));
        }

        event.setCancelled(true);
    }

    private String format(Block block) {
        return block.getWorld().getName() + " " + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
