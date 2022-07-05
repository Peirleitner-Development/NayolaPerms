package net.nayola.nayolaperms.command;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.nayola.core.NayolaCore;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermGroup;

public class CommandGroups implements CommandExecutor {

	public CommandGroups() {
		NayolaPerms.getInstance().getCommand("groups").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs,
				"command.groups.list", null, true);

		Collection<PermGroup> groups = NayolaPerms.getInstance().getPermissionManager().getGroupsInOrder();

		if (!groups.isEmpty()) {

			groups.forEach(group -> {
				NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs,
						"command.groups.group", Arrays.asList(group.getColoredName()), true);
			});

		}

		return true;
	}

}
