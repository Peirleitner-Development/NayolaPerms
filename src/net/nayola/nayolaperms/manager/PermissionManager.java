package net.nayola.nayolaperms.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

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
		
		// Load Data
		this.loadGroupsFromDatabase();

	}

	public Collection<PermGroup> getGroups() {
		return groups;
	}
	
	public List<PermGroup> getGroupsInOrder() {
		
		List<Integer> list = new ArrayList<>();
		
		for(PermGroup group : this.getGroups()) {
			list.add(group.getPriority());
		}
		
		Collections.sort(list, Collections.reverseOrder());
		
		List<PermGroup> groups = new ArrayList<>();
		
		for(int i : list) {
			
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG, "looking for i = " + i);
			PermGroup g = this.getGroupByPriority(i);
			
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG, "group is " + (g == null ? "null" : g.getName()));
			
			groups.add(g);
			
		}
		
		return groups;
		
	}
	
	public final PermGroup getGroupByID(@Nonnull int id) {
		return this.getGroups().stream().filter(group -> group.getID() == id).findAny().orElse(null);
	}

	/**
	 * 
	 * @param name - Name of the group
	 * @return Group with the given name (not case-sensitive) or <code>null</code>
	 *         if none can be found
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getGroupByDisplayName(String)
	 */
	public final PermGroup getGroupByName(@Nonnull String name) {
		return this.getGroups().stream().filter(group -> group.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	/**
	 * 
	 * @param name - DisplayName of the group
	 * @return Group with the given displayname (not case-sensitive) or
	 *         <code>null</code> if none can be found
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @see #getGroupByName(String)
	 */
	public final PermGroup getGroupByDisplayName(@Nonnull String name) {
		return this.getGroups().stream().filter(group -> group.getDisplayName().equalsIgnoreCase(name)).findAny()
				.orElse(null);
	}
	
	public final PermGroup getGroupByPriority(@Nonnull int priority) {
		return this.getGroups().stream().filter(group -> group.getPriority() == priority).findAny().orElse(null);
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

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + NayolaPerms.table_players + " WHERE uuid = ?");
			stmt.setString(1, uuid.toString());

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				// 1 = UUID
				int groupID = rs.getInt(2);
				long expire = rs.getLong(3);

				PermPlayer pp = new PermPlayer(uuid, groupID, expire);

				NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG,
						"Returned PermPlayer '" + uuid.toString() + "' from Database.");
				return pp;

			} else {
				NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG,
						"Could not get PermPlayer by UUID '" + uuid.toString() + "' from Database: None found.");
				return null;
			}

		} catch (SQLException e) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.ERROR,
					"Could not get PermPlayer '" + uuid.toString() + "' from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean createPlayer(@Nonnull UUID uuid) {

		PermGroup defaultGroup = this.getDefaultGroup();

		if (defaultGroup == null) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.INFO,
					"Could not create Player '" + uuid.toString() + "': Could not validate default group");
			return false;
		}

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + NayolaPerms.table_players + " (uuid, groupID, expire) VALUES (?, ?, ?);");
			stmt.setString(1, uuid.toString());
			stmt.setInt(2, defaultGroup.getID());
			stmt.setLong(3, -1);

			stmt.execute();

			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG,
					"Created Player '" + uuid.toString() + "'.");
			return true;

		} catch (SQLException e) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.ERROR,
					"Could not create Player '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final PermGroup getGroupByResultSet(@Nonnull ResultSet rs) throws SQLException {
		
		int id = rs.getInt(1);
		String name = rs.getString(2);
		String displayName = rs.getString(3);
		Material icon = Material.valueOf(rs.getString(4));
		String hexColor = rs.getString(5);
		boolean isDefault = rs.getBoolean(6);
		int priority = rs.getInt(7);
		
		PermGroup group = new PermGroup();
		group.setID(id);
		group.setName(name);
		group.setDisplayName(displayName);
		group.setIcon(icon);
		group.setHexColor(hexColor);
		group.setDefault(isDefault);
		group.setPriority(priority);
		
		return group;
		
	}
	
	public final void loadGroupsFromDatabase() {
		
		NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.INFO, "Loading Groups from Database..");
		this.getGroups().clear();
		
		try {
			
			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement("SELECT * FROM " + NayolaPerms.table_groups);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				
				PermGroup group = this.getGroupByResultSet(rs);
				
				if(group != null) this.getGroups().add(group);
				
			}
			
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.INFO, "Loaded " + this.getGroups().size() + " Groups from Database.");
			
		} catch (SQLException e) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.ERROR, "Could not load groups from database/SQL: " + e.getMessage());
		}
		
	}
	
	public final boolean createGroup(@Nonnull CommandSender cs, @Nonnull PermGroup group) {

		PermGroup existing = this.getGroupByName(group.getName());

		// Return if the group does already exist
		if (existing != null) {
			NayolaCore.getInstance().getLanguageManagerSpigot().sendMessage(NayolaPerms.getInstance(), cs,
					"command.nayolaperms.group.create.error.name-already-exists", Arrays.asList(existing.getName()),
					true);
			return false;
		}

		try {
			
			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement("INSERT INTO " + NayolaPerms.table_groups + " (name, displayName, icon, hexColor, isDefault, priority) VALUES (?, ?, ?, ?, ?, ?);");
			stmt.setString(1, group.getName());
			stmt.setString(2, group.getDisplayName());
			stmt.setString(3, group.getIcon().toString());
			stmt.setString(4, group.getHexColor());
			stmt.setBoolean(5, group.isDefault());
			stmt.setInt(6, group.getPriority());
			
			stmt.execute();
			
			ResultSet rs = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement("SELECT LAST_INSERT_ID();").executeQuery();
			
			if(rs.next()) {
				group.setID(rs.getInt(1));
			} else {
				NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.DEBUG, "Could not get last insert id for group '" + group.getName() + "': Reloading all data from database..");
				this.loadGroupsFromDatabase();
			}
			
			cs.sendMessage("group created: " + group.getName());
			return true;
			
		} catch (SQLException e) {
			NayolaCore.getInstance().logSpigot(NayolaPerms.getInstance(), LogType.ERROR, "Could not create group '" + group.getName() + "'/SQL: " + e.getMessage());
			return false;
		}
		
	}

	public final PermGroup getDefaultGroup() {
		return this.getGroups().stream().filter(group -> group.isDefault()).findAny().orElse(null);
	}

	public Collection<PermPermission> getPermissions() {
		return permissions;
	}

}
