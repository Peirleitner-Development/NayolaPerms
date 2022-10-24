package at.peirleitner.nayolaperms.command;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.user.PredefinedMessage;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.permission.PermPlayer;

public class CommandRank implements CommandExecutor {

	public CommandRank() {
		NayolaPerms.getInstance().getCommand("rank").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {

		if (!(cs instanceof Player)) {
			cs.sendMessage(
					Core.getInstance().getLanguageManager().getMessage(PredefinedMessage.ACTION_REQUIRES_PLAYER));
			return true;
		}

		Player p = (Player) cs;
		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(p.getUniqueId());
		PermGroup group = pp.getGroup();

		if (pp.isSetToExpire()) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.rank.temporary", Arrays.asList(group.getName(), pp.getExpireDateAsString()), true);
		} else {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.rank.permanent", Arrays.asList(group.getName()), true);
		}

		return true;
	}

}
