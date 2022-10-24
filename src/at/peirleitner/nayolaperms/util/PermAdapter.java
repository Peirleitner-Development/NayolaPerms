package at.peirleitner.nayolaperms.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.permission.PermPlayer;

public class PermAdapter extends PermissibleBase {

	private Player p;

	public PermAdapter(Player opable) {
		super(opable);
		this.p = opable;
	}

	@Override
	public boolean hasPermission(String permission) {

		Bukkit.broadcastMessage("A");

		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayer(p.getUniqueId());

		PermGroup requiredGroup = NayolaPerms.getInstance().getPermissionManager()
				.getRequiredGroupForPermission(permission);

		if (requiredGroup != null) {

			if (pp.getGroup().getPriority() >= requiredGroup.getPriority()) {
				p.sendMessage("you have the perm " + permission);
				return true;
			} else {
				p.sendMessage("This requires the group " + requiredGroup.getName() + " for perm " + permission);
				return false;
			}

		} else {
			Core.getInstance().log(this.getClass(), LogType.DEBUG,
					"Permission '" + permission + "' isn't saved in any group.");
		}

		return super.hasPermission(permission);
	}

}