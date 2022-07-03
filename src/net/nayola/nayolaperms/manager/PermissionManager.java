package net.nayola.nayolaperms.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.nayola.core.NayolaCore;
import net.nayola.core.util.LogType;
import net.nayola.nayolaperms.NayolaPerms;
import net.nayola.nayolaperms.permission.PermGroup;
import net.nayola.nayolaperms.permission.PermPermission;
import net.nayola.nayolaperms.permission.PermPlayer;

public class PermissionManager {

	private Collection<PermGroup> groups;
	private Collection<PermPlayer> players;
	private Collection<PermPermission> permissions;

	public PermissionManager() {

		// Initialize
		this.groups = new ArrayList<>();
		this.players = new ArrayList<>();
		this.permissions = new ArrayList<>();

	}

	public Collection<PermGroup> getGroups() {
		return groups;
	}

	public Collection<PermPlayer> getPlayers() {
		return players;
	}
	
	public final PermPlayer getPlayer(@Nonnull UUID uuid) {
		return this.getPlayerFromCache(uuid) == null ? this.getPlayerFromDatabase(uuid) : this.getPlayerFromCache(uuid);
	}
	
	private final PermPlayer getPlayerFromCache(@Nonnull UUID uuid) {
		return this.getPlayers().stream().filter(player -> player.getUUID().equals(uuid)).findAny().orElse(null);
	}
	
	private final PermPlayer getPlayerFromDatabase(@Nonnull UUID uuid) {
		
		try {
			
			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + NayolaPerms.table_players + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());
			
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				
				// 1 = UUID
				int groupID = rs.getInt(2);
				long expire = rs.getLong(3);
				
				PermPlayer pp = new PermPlayer(uuid, groupID, expire);
				
				NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG, "Returned PermPlayer '" + uuid.toString() + "' from Database.");
				return pp;
				
			} else {
				NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG, "Could not get PermPlayer by UUID '" + uuid.toString() + "' from Database: None found.");
				return null;
			}
			
		} catch (SQLException e) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.ERROR, "Could not get PermPlayer '" + uuid.toString() + "' from Database/SQL: " + e.getMessage());
			return null;
		}
		
	}

	public Collection<PermPermission> getPermissions() {
		return permissions;
	}

}
