package net.nayola.nayolaperms.util;

import javax.annotation.Nonnull;

public enum NayolaPermission {

	COMMAND_NAYOLA_PERMS_USE("command.nayolaperms.use");
	
	private String permission;
	
	private NayolaPermission(@Nonnull String permission) {
		this.permission = permission;
	}
	
	public final String getPermission() {
		return this.permission;
	}

}
