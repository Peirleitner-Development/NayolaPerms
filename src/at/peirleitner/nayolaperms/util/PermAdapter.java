package at.peirleitner.nayolaperms.util;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

public class PermAdapter extends PermissibleBase {

	public PermAdapter(ServerOperator opable) {
		super(opable);
		// TODO Auto-generated constructor stub
	}

	private String permission;
	private boolean is;

	@Override
	public boolean isPermissionSet(Permission perm) {
		this.permission = perm.toString();
		this.is = super.isPermissionSet(perm);
		return is;
	}

	@Override
	public boolean hasPermission(String inName) {
		this.permission = inName;
		this.is = super.hasPermission(inName);
		return is;
	}

	public final String getPermission() {
		return permission;
	}

	public final boolean isSet() {
		return is;
	}

}