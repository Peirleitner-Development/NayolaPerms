package at.peirleitner.nayolaperms.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.nayolaperms.NayolaPerms;

public class PlayerJoinListener implements Listener {

	public PlayerJoinListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();
		this.sendRankInfo(p);
		NayolaPerms.getInstance().getPermissionManager().reloadPermissions(p);

	}

	private final void sendRankInfo(Player p) {

		if (!Core.getInstance().getSettingsManager().isSetting(NayolaPerms.getInstance().getPluginName(),
				"send-rank-info-on-join"))
			return;

		new BukkitRunnable() {

			@Override
			public void run() {

				if (p == null) {
					this.cancel();
					return;
				}

				p.performCommand("rank");

			}
		}.runTaskLater(NayolaPerms.getInstance(), 20L * 3);

	}

}
