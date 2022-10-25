package at.peirleitner.nayolaperms.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.user.User;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermPlayer;

public class AsyncPlayerPreLoginListener implements Listener {

	public AsyncPlayerPreLoginListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

		if (NayolaPerms.getInstance().getMySQL() == null || !NayolaPerms.getInstance().getMySQL().isConnected()) {
			e.disallow(Result.KICK_OTHER, "Could not validate connection towards the permission database.");
			return;
		}

		User user = Core.getInstance().getUserSystem().getUser(e.getUniqueId());
		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(e.getUniqueId());

		if (pp == null) {

			if (!NayolaPerms.getInstance().getPermissionManager().createPlayer(e.getUniqueId())) {

				String result = Core.getInstance().getLanguageManager().getMessage(
						NayolaPerms.getInstance().getPluginName(), user.getLanguage(),
						"listener.async-player-pre-login.error.could-not-create-player", null);
				e.disallow(Result.KICK_OTHER, result);

			}

		}

		if (!NayolaPerms.getInstance().getPermissionManager().getPlayers().contains(pp)) {
			NayolaPerms.getInstance().getPermissionManager().getPlayers().add(pp);
		}

	}

}
