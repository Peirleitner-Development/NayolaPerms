package net.nayola.nayolaperms.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import net.nayola.core.NayolaCore;
import net.nayola.core.util.LogType;
import net.nayola.core.util.NetworkUser;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermPlayer;

public class AsyncPlayerPreLoginListener implements Listener {

	public AsyncPlayerPreLoginListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {

		NetworkUser user = NayolaCore.getInstance().getUserSystem().getUser(e.getUniqueId());

		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(e.getUniqueId());

		if (pp != null && !NayolaPerms.getInstance().getPermissionManager().getPlayers().contains(pp)) {
			NayolaPerms.getInstance().getPermissionManager().getPlayers().add(pp);
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG,
					"Cached Player on join: " + e.getUniqueId().toString());
		} else {

			if (!NayolaPerms.getInstance().getPermissionManager().createPlayer(e.getUniqueId())) {

				String result = NayolaCore.getInstance().getLanguageManagerSpigot().getMessage(
						NayolaPerms.getInstance(), "listener.join.error.could-not-create-player", user.getLanguage(),
						null);
				e.disallow(Result.KICK_OTHER, result);

			}

		}

	}

}
