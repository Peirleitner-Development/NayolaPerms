package at.peirleitner.nayolaperms.command;

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.util.NayolaPermission;

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

		} else if (args.length == 6) {

			if (args[0].equalsIgnoreCase("group")) {

				if (args[1].equalsIgnoreCase("create")) {

					try {

						String name = args[2];
						Material icon = Material.valueOf(args[3]);
						boolean isDefault = Boolean.valueOf(args[4]);
						int priority = Integer.valueOf(args[5]);

						PermGroup group = new PermGroup();
						group.setName(name);
						group.setIcon(icon);
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
		Core.getInstance().getLanguageManager().sendMessage( cs,NayolaPerms.getInstance().getPluginName(),
				"command.nayolaperms.syntax", Arrays.asList(NayolaPerms.getInstance().getDescription().getVersion()),
				true);
	}

}
