package at.peirleitner.nayolaperms.permission;

import at.peirleitner.nayolaperms.NayolaPerms;

/**
 * This class represents a permission that belongs to a {@link PermGroup}.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class PermPermission {

	private String permission;
	private int groupID;
	private int serverID;

	public PermPermission(String permission, int groupID, int serverID) {
		this.permission = permission;
		this.groupID = groupID;
		this.serverID = serverID;
	}

	/**
	 * 
	 * @return Permission string
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * 
	 * @return ID of the {@link PermGroup} that this permission belongs to
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final int getGroupID() {
		return groupID;
	}

	/**
	 * 
	 * @return Group gained with the ID of {@link #getGroupID()}
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final PermGroup getGroup() {
		return NayolaPerms.getInstance().getPermissionManager().getGroupByID(this.getGroupID());
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public final int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}
	
	@Override
	public String toString() {
		return "PermPermission[permission=" + this.getPermission() + ",groupID=" + this.getGroupID() + ",saveType=" + this.getServerID() + "]";
	}

}
