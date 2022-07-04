package net.nayola.nayolaperms.command;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.nayola.core.NayolaCore;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermGroup;
import net.nayola.nayolaperms.permission.PermPlayer;

public class CommandRank implements CommandExecutor {

	public CommandRank() {
		NayolaPerms.getInstance().getCommand("rank").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg, String[] args) {
		
		if(!(cs instanceof Player)) {
			cs.sendMessage(NayolaCore.getInstance().getLanguageManagerSpigot().getNoConsoleMessage());
			return true;
		}
		
		Player p = (Player) cs;
		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(p.getUniqueId());
		PermGroup group = pp.getGroup();
		
		if(pp.isSetToExpire()) {
			NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs, "command.rank.temporary", Arrays.asList(group.getColoredName(), pp.getExpireDateAsString()), true);
		} else {
			NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs, "command.rank.permanent", Arrays.asList(group.getColoredName()), true);
		}
		
		return true;
	}

}
