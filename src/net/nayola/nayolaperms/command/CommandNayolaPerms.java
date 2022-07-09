package net.nayola.nayolaperms.command;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.nayola.core.NayolaCore;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermGroup;
import net.nayola.nayolaperms.util.NayolaPermission;

public class CommandNayolaPerms implements CommandExecutor {

	public CommandNayolaPerms() {
		NayolaPerms.getInstance().getCommand("nayolaperms").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!cs.hasPermission(NayolaPermission.COMMAND_NAYOLA_PERMS_USE.getPermission())) {
			cs.sendMessage("no perm");
			return true;
		}

		// Yes, switch case would be more efficient but by far less readable

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("reload")) {

				NayolaPerms.getInstance().getPermissionManager().loadGroupsFromDatabase();
				return true;

			} else if(args[0].equalsIgnoreCase("loadDefaultGroups")) {
				
				NayolaPerms.getInstance().getPermissionManager().loadDefaultGroups(cs);
				return true;
				
			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 3) {

		} else if (args.length == 4) {

		} else if (args.length == 5) {

		} else if (args.length == 8) {

			if (args[0].equalsIgnoreCase("group")) {

				if (args[1].equalsIgnoreCase("create")) {

					try {

						String name = args[2];
						String displayName = args[3];
						Material icon = Material.valueOf(args[4]);
						String color = args[5];
						boolean isDefault = Boolean.valueOf(args[6]);
						int priority = Integer.valueOf(args[7]);

						PermGroup group = new PermGroup();
						group.setName(name);
						group.setDisplayName(displayName);
						group.setIcon(icon);
						group.setHexColor(color);
						group.setDefault(isDefault);
						group.setPriority(priority);

						NayolaPerms.getInstance().getPermissionManager().createGroup(cs, group);

					} catch (IllegalArgumentException ex) {
						cs.sendMessage("error in syntax");
						this.sendHelp(cs);
						return true;
					}

					return true;

				} else {
					this.sendHelp(cs);
					return true;
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else {
			this.sendHelp(cs);
			return true;
		}

		return true;
	}

	private final void sendHelp(@Nonnull CommandSender cs) {
		NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs,
				"command.nayolaperms.syntax", Arrays.asList(NayolaPerms.getInstance().getDescription().getVersion()),
				true);
	}

}
