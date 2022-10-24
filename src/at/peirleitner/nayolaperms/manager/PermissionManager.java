package at.peirleitner.nayolaperms.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.LogType;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.permission.PermPermission;
import at.peirleitner.nayolaperms.permission.PermPlayer;

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

		for (PermGroup group : this.getGroups()) {
			list.add(group.getPriority());
		}

		Collections.sort(list, Collections.reverseOrder());

		List<PermGroup> groups = new ArrayList<>();

		for (int i : list) {

//			Core.getInstance().log(this.getClass(), LogType.DEBUG, "looking for i = " + i);
			PermGroup g = this.getGroupByPriority(i);

//			Core.getInstance().log(this.getClass(), LogType.DEBUG,
//					"group is " + (g == null ? "null" : g.getName()));

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

	public final PermGroup getGroupByIcon(@Nonnull Material icon) {
		return this.getGroups().stream().filter(group -> group.getIcon().equals(icon)).findAny().orElse(null);
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

				Core.getInstance().log(this.getClass(), LogType.DEBUG,
						"Returned PermPlayer '" + uuid.toString() + "' from Database.");
				return pp;

			} else {
				Core.getInstance().log(this.getClass(), LogType.DEBUG,
						"Could not get PermPlayer by UUID '" + uuid.toString() + "' from Database: None found.");
				return null;
			}

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not get PermPlayer '" + uuid.toString() + "' from Database/SQL: " + e.getMessage());
			return null;
		}

	}

	public final boolean createPlayer(@Nonnull UUID uuid) {

		PermGroup defaultGroup = this.getDefaultGroup();

		if (defaultGroup == null) {
			Core.getInstance().log(this.getClass(), LogType.INFO,
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

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Created Player '" + uuid.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not create Player '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final PermGroup getGroupByResultSet(@Nonnull ResultSet rs) throws SQLException {

		int id = rs.getInt(1);
		String name = rs.getString(2);
		Material icon = Material.valueOf(rs.getString(3));
		boolean isDefault = rs.getBoolean(4);
		int priority = rs.getInt(5);

		PermGroup group = new PermGroup();
		group.setID(id);
		group.setName(name);
		group.setIcon(icon);
		group.setDefault(isDefault);
		group.setPriority(priority);

		return group;

	}

	public final void loadGroupsFromDatabase() {

		if(!NayolaPerms.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Not loading groups from Database because no connection has been established.");
			return;
		}
		
		Core.getInstance().log(this.getClass(), LogType.INFO, "Loading Groups from Database..");
		this.getGroups().clear();

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + NayolaPerms.table_groups);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				PermGroup group = this.getGroupByResultSet(rs);

				if (group != null)
					this.getGroups().add(group);

			}

			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Loaded " + this.getGroups().size() + " Groups from Database.");

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not load groups from database/SQL: " + e.getMessage());
		}

	}

	/**
	 * Create default groups based on the nayola server network
	 * 
	 * @param cs - CommandSender creating the groups
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 */
	public final void loadDefaultGroups(@Nonnull CommandSender cs) {

		if (!this.groups.isEmpty()) {
			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Can't load default groups: At least one group does already exist.");
			return;
		}

		List<PermGroup> groups = new ArrayList<>();

		// Add Groups
		// Staff
		groups.add(new PermGroup("Owner", Material.NETHERITE_PICKAXE, false, 490));
		groups.add(new PermGroup("Leadership",  Material.DIAMOND_PICKAXE, false, 480));
		groups.add(new PermGroup("Admin",Material.DIAMOND_CHESTPLATE, false, 470));
		groups.add(new PermGroup("Developer", Material.LAVA_BUCKET, false, 460));
		groups.add(new PermGroup("Content", Material.WRITABLE_BOOK, false, 450));
		groups.add(new PermGroup("Builder", Material.GOLDEN_AXE, false, 440));
		groups.add(new PermGroup("Moderator", Material.GOLDEN_SWORD, false, 430));
		groups.add(new PermGroup("Supporter", Material.STONE_SWORD, false, 420));
		groups.add(new PermGroup("Trainee", Material.WOODEN_SHOVEL,  false, 410));
		// Special
		groups.add(new PermGroup("VIP", Material.SHULKER_SHELL, false, 310));
		// Premium
		groups.add(new PermGroup("Emerald",  Material.EMERALD, false, 230));
		groups.add(new PermGroup("Diamond",  Material.DIAMOND, false, 220));
		groups.add(new PermGroup("Gold",  Material.RAW_GOLD,false, 210));
		// User
		groups.add(new PermGroup("Iron",  Material.RAW_IRON, false, 130));
		groups.add(new PermGroup("Coal",  Material.COAL,false, 120));
		groups.add(new PermGroup("Stone",  Material.STONE,  true, 110));

		// Create Groups
		for (PermGroup group : groups) {
			this.createGroup(cs, group);
		}

		this.loadGroupsFromDatabase();

	}

	public final boolean createGroup(@Nonnull CommandSender cs, @Nonnull PermGroup group) {

		PermGroup existing = this.getGroupByName(group.getName());

		// Return if the group does already exist (by name)
		if (this.getGroupByName(group.getName()) != null) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.group.create.error.name-already-exists", Arrays.asList(existing.getName()),
					true);
			return false;
		}

		// Return if the group does already exist (by icon)
		if (this.getGroupByIcon(group.getIcon()) != null) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.group.create.error.icon-already-exists",
					Arrays.asList(existing.getIcon().toString()), true);
			return false;
		}

		// Return if the group does already exist (by priority)
		if (this.getGroupByPriority(group.getPriority()) != null) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.group.create.error.priority-already-exists",
					Arrays.asList("" + existing.getPriority()), true);
			return false;
		}

		// Return if a default group does already exist
		if (group.isDefault() && this.getDefaultGroup() != null) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.group.create.error.default-group-already-exists",
					Arrays.asList(this.getDefaultGroup().getName()), true);
			return false;
		}

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("INSERT INTO " + NayolaPerms.table_groups
							+ " (name, icon, isDefault, priority) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, group.getName());
			stmt.setString(2, group.getIcon().toString());
			stmt.setBoolean(3, group.isDefault());
			stmt.setInt(4, group.getPriority());

			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();

			if (rs.next()) {
				group.setID(rs.getInt(1));
			} else {
				Core.getInstance().log(this.getClass(), LogType.DEBUG, "Could not get last insert id for group '"
						+ group.getName() + "': Reloading all data from database..");
				this.loadGroupsFromDatabase();
			}

			cs.sendMessage("group created: " + group.getName());
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not create group '" + group.getName() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final PermGroup getDefaultGroup() {
		return this.getGroups().stream().filter(group -> group.isDefault()).findAny().orElse(null);
	}

	public final Collection<PermPermission> getPermissions() {
		return permissions;
	}

	public final Collection<PermPermission> getPermissions(@Nonnull PermGroup group) {

		Collection<PermPermission> permList = new ArrayList<>();

		this.getPermissions().forEach(permission -> {
			if (permission.getGroupID() == group.getID()) {
				permList.add(permission);
			}
		});

		return permList;
	}

	public final PermGroup getRequiredGroupForPermission(@Nonnull String permission) {

		PermGroup group = null;

		for (PermGroup pg : this.getGroupsInOrder()) {

			for (PermPermission pp : pg.getPermissions()) {

				if (pp.getPermission().equalsIgnoreCase(permission)) {
					return group;
				}

			}

		}

		return group;
	}

}
