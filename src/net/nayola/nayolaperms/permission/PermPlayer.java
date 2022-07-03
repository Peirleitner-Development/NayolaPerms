package net.nayola.nayolaperms.permission;

import java.util.UUID;

/**
 * This class represents a player on the server
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 */
public class PermPlayer {

	private UUID uuid;
	private int groupID;
	private long expire;

	public PermPlayer(UUID uuid, int groupID, long expire) {
		this.uuid = uuid;
		this.groupID = groupID;
		this.expire = expire;
	}

	/**
	 * 
	 * @return UUID of the player
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * 
	 * @return ID of the {@link PermGroup} that this player belongs to
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
		return null;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	/**
	 * 
	 * @return Timestamp when the current rank expires
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote Setting this to <b>-1</b> results in the rank never expiring
	 */
	public final long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}