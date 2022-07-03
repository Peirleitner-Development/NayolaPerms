package net.nayola.nayolaperms.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermPlayer;

public class PlayerQuitListener implements Listener {

	public PlayerQuitListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {

		Player p = e.getPlayer();

		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(p.getUniqueId());

		if (pp != null) {
			NayolaPerms.getInstance().getPermissionManager().getPlayers().remove(pp);
		}

	}

}