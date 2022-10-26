package at.peirleitner.nayolaperms.command;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.core.util.user.User;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.manager.PermissionManager;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.permission.PermPermission;
import at.peirleitner.nayolaperms.permission.PermPlayer;
import at.peirleitner.nayolaperms.util.NayolaPermission;
import net.md_5.bungee.api.ChatColor;

public class CommandNayolaPerms implements CommandExecutor {

	public CommandNayolaPerms() {
		NayolaPerms.getInstance().getCommand("nayolaperms").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (cs instanceof Player && args.length == 0) {

			Player p = (Player) cs;
			PermPlayer pp = this.getPermissionManager().getPlayer(p.getUniqueId());

			for (PermPermission perm : this.getPermissionManager().getPermissions()) {
				cs.sendMessage((pp.hasPermission(perm.getPermission()) ? ChatColor.GREEN : ChatColor.RED) + "("
						+ perm.getGroup().getName() + "): " + perm.getPermission());
			}

			return true;
		}

		if (!cs.hasPermission(NayolaPermission.COMMAND_NAYOLA_PERMS_USE.getPermission())) {
			cs.sendMessage(Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NO_PERMISSION));
			return true;
		}

		// Yes, switch case would be more efficient but by far less readable

		if (args.length == 1) {

			if (args[0].equalsIgnoreCase("reload")) {

				NayolaPerms.getInstance().getPermissionManager().reload();
				return true;

			} else if (args[0].equalsIgnoreCase("loadDefaultGroups")) {

				this.getPermissionManager().loadDefaultGroups(cs);
				return true;

			} else {
				this.sendHelp(cs);
				return true;
			}

		} else if (args.length == 3) {

			if (args[0].equalsIgnoreCase("group")) {

				if (args[1].equalsIgnoreCase("get")) {

					User target = Core.getInstance().getUserSystem().getByLastKnownName(args[2]);

					if (target == null) {
						cs.sendMessage(
								Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.NOT_REGISTERED));
						return true;
					}

					PermPlayer pp = this.getPermissionManager().getPlayer(target.getUUID());

					if (pp == null) {
						Core.getInstance().getLanguageManager().sendMessage(cs,
								NayolaPerms.getInstance().getPluginName(),
								"command.nayolaperms.main.error.player-has-no-profile",
								Arrays.asList(target.getDisplayName()), true);
						return true;
					}

					PermGroup pg = pp.getGroup();

					Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
							"command.nayolaperms.group.get.success",
							Arrays.asList(target.getDisplayName(), pg.getDisplayName()), true);
					return true;

				} else {
					this.sendHelp(cs);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("permission")) {

				if (args[1].equalsIgnoreCase("get")) {

					PermGroup pg = this.getPermissionManager().getGroupByName(args[2]);

					if (pg == null) {
						Core.getInstance().getLanguageManager().sendMessage(cs,
								NayolaPerms.getInstance().getPluginName(),
								"command.nayolaperms.main.error.group-does-not-exist-name", Arrays.asList(args[2]),
								true);
						return true;
					}

					Collection<PermPermission> permissions = this.getPermissionManager()
							.getPermissionsFromAllGroups(pg);

					if (permissions.isEmpty()) {
						Core.getInstance().getLanguageManager().sendMessage(cs,
								NayolaPerms.getInstance().getPluginName(),
								"command.nayolaperms.permission.get.success.no-permissions",
								Arrays.asList(pg.getDisplayName()), true);
						return true;
					}

					Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
							"command.nayolaperms.permission.get.success.pre-text",
							Arrays.asList(pg.getDisplayName(), "" + permissions.size()), true);

					Player p = null;

					if (cs instanceof Player) {
						p = (Player) cs;
					}

					String message = null;

					for (PermPermission pp : permissions) {

						message = Core.getInstance().getLanguageManager().getMessage(
								NayolaPerms.getInstance().getPluginName(),
								p == null ? Core.getInstance().getDefaultLanguage()
										: Core.getInstance().getUserSystem().getUser(p.getUniqueId()).getLanguage(),
								"command.nayolaperms.permission.get.success.permission", null);
						
						message = message.replace("{0}", pp.getPermission());
						message = message.replace("{1}", pp.getGroup().getDisplayName());

						cs.sendMessage(message);

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

		} else if (args.length == 4) {

			if (args[0].equalsIgnoreCase("group")) {

				if (args[1].equalsIgnoreCase("set")) {

				} else {
					this.sendHelp(cs);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("permission")) {

				if (args[1].equalsIgnoreCase("add")) {

					// Command: /np permission add <Group> <Perm>
					int groupID = Integer.valueOf(args[2]);
					String permission = args[3];

					this.getPermissionManager().addPermission(cs, groupID, permission);
					return true;

				} else if (args[1].equalsIgnoreCase("remove")) {

				} else {
					this.sendHelp(cs);
					return true;
				}

			} else {
				this.sendHelp(cs);
				return true;
			}

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

				} else if (args[1].equalsIgnoreCase("set")) {

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
		Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
				"command.nayolaperms.syntax", Arrays.asList(NayolaPerms.getInstance().getDescription().getVersion()),
				true);
	}

	private final PermissionManager getPermissionManager() {
		return NayolaPerms.getInstance().getPermissionManager();
	}

}
