package at.peirleitner.nayolaperms.command;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;

public class CommandGroups implements CommandExecutor {

	public CommandGroups() {
		NayolaPerms.getInstance().getCommand("groups").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
				"command.groups.list", null, true);

		Collection<PermGroup> groups = NayolaPerms.getInstance().getPermissionManager().getGroupsInOrder();

		if (!groups.isEmpty()) {

			groups.forEach(group -> {
				Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
						"command.groups.group", Arrays.asList(group.getDisplayName()), true);
			});

		}

		return true;
	}

}
