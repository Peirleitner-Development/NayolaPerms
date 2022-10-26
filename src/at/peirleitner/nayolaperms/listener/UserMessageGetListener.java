package at.peirleitner.nayolaperms.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import at.peirleitner.core.Core;
import at.peirleitner.core.api.local.UserMessageGetEvent;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;

public class UserMessageGetListener implements Listener {

	public UserMessageGetListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}
	
	public static HashMap<UUID, String> tried = new HashMap<>();

	@EventHandler
	public void onUserMessageGet(UserMessageGetEvent e) {

		if (e.getPredefinedMessage() == PredefinedMessage.NO_PERMISSION) {

			PermGroup pg = NayolaPerms.getInstance().getPermissionManager().getGroupByID(1);

			e.setMessage(Core.getInstance().getLanguageManager().getPrefix(NayolaPerms.getInstance().getPluginName(),
					Core.getInstance().getDefaultLanguage())
					+ Core.getInstance().getLanguageManager().getMessage(NayolaPerms.getInstance().getPluginName(),
							Core.getInstance().getDefaultLanguage(), "main.no-permission",
							Arrays.asList(pg.getDisplayName().toUpperCase())));
		}

	}

}
