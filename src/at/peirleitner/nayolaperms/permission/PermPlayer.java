package at.peirleitner.nayolaperms.permission;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import at.peirleitner.core.util.GlobalUtils;
import at.peirleitner.nayolaperms.NayolaPerms;

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
		return NayolaPerms.getInstance().getPermissionManager().getGroupByID(this.getGroupID());
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

	public final String getExpireDateAsString() {
		return GlobalUtils.getFormatedDate(this.getExpire());
	}

	public final boolean isSetToExpire() {
		return this.getExpire() != -1;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public Collection<PermPermission> getPermissions() {
		return this.getGroup().getPermissions();
	}
	
	public final boolean hasPermission(@Nonnull String perm) {
		return NayolaPerms.getInstance().getPermissionManager().hasPermission(this.getGroup(), perm);
	}
	
	public final Player getBukkitPlayer() {
		return Bukkit.getPlayer(this.getUUID());
	}
	
	public final boolean reloadPermissions() {
		return NayolaPerms.getInstance().getPermissionManager().reloadPermissions(this.getBukkitPlayer());
	}

	@Override
	public String toString() {
		return "PermPlayer[uuid=" + this.getUUID().toString() + ",groupID=" + this.getGroupID() + ",expire="
				+ this.getExpire() + "]";
	}

}
