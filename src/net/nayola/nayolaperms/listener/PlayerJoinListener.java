package net.nayola.nayolaperms.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.nayola.core.NayolaCore;
import net.nayola.core.util.LogType;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermPlayer;

public class PlayerJoinListener implements Listener {

	public PlayerJoinListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();

		new BukkitRunnable() {

			@Override
			public void run() {

				PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(p.getUniqueId());

				if (pp != null && !NayolaPerms.getInstance().getPermissionManager().getPlayers().contains(pp)) {
					NayolaPerms.getInstance().getPermissionManager().getPlayers().add(pp);
					NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG,
							"Cached Player on join: " + p.getUniqueId().toString());
				}

			}
		}.runTaskAsynchronously(NayolaPerms.getInstance());

	}

}
