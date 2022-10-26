package at.peirleitner.nayolaperms.manager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import at.peirleitner.core.Core;
import at.peirleitner.core.SpigotMain;
import at.peirleitner.core.util.LogType;
import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermGroup;
import at.peirleitner.nayolaperms.permission.PermPermission;
import at.peirleitner.nayolaperms.permission.PermPlayer;
import net.md_5.bungee.api.ChatColor;

public class PermissionManager {

	private Collection<PermGroup> groups;
	private Collection<PermPlayer> players;
	private Collection<PermPermission> permissions;
	private HashMap<UUID, PermissionAttachment> attachments;

	private final String CANT_UPDATE_PERM_ATTACHMENTS_KICKED = ChatColor.RED + "Could not reload your permission attachments, please connect again.";
	private final String PERMISSION_REMOVE_RESULT_SET_EMPTY = ChatColor.RED + "Row count on removed permissions returned 0. Manually check permissions inside '" + NayolaPerms.table_permissions + "' table.";
	
	public PermissionManager() {

		// Initialize
		this.groups = new ArrayList<>();
		this.players = new ArrayList<>();
		this.permissions = new ArrayList<>();
		this.attachments = new HashMap<>();

		// Load Data
		this.reload();

	}

	public HashMap<UUID, PermissionAttachment> getAttachments() {
		return this.attachments;
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

	public List<PermGroup> getGroupsInReverseOrder() {

		List<Integer> list = new ArrayList<>();

		for (PermGroup group : this.getGroups()) {
			list.add(group.getPriority());
		}

		Collections.sort(list);

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
//				Core.getInstance().log(this.getClass(), LogType.DEBUG,
//						"Could not get PermPlayer by UUID '" + uuid.toString() + "' from Database: None found.");
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

		PermPlayer pp = new PermPlayer(uuid, defaultGroup.getID(), -1);

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + NayolaPerms.table_players + " (uuid, groupID, expire) VALUES (?, ?, ?);");
			stmt.setString(1, pp.getUUID().toString());
			stmt.setInt(2, pp.getGroupID());
			stmt.setLong(3, pp.getExpire());

			stmt.executeUpdate();

			if (NayolaPerms.getInstance().isCachingEnabled()) {
				this.getPlayers().add(pp);
			}

			Core.getInstance().log(this.getClass(), LogType.DEBUG, "Created Player '" + uuid.toString() + "'.");
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not create Player '" + uuid.toString() + "'/SQL: " + e.getMessage());
			return false;
		}

	}

	public final boolean setGroup(@Nonnull PermPlayer pp, @Nonnull PermGroup group) {

		if (pp.getGroupID() == group.getID()) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Did not update group of player '"
					+ pp.getUUID().toString() + "' because it is already set to '" + group.getName() + "'.");
			return false;
		}
		
		PermGroup current = pp.getGroup();

		try {
			
			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement("UPDATE " + NayolaPerms.table_players + " SET groupID = ? WHERE uuid = ?");
			stmt.setInt(1, group.getID());
			stmt.setString(2, pp.getUUID().toString());
			
			stmt.executeUpdate();
			
			if(NayolaPerms.getInstance().isCachingEnabled()) {
				pp.setGroupID(group.getID());
				pp.reloadPermissions();
			}
			
			Core.getInstance().log(getClass(), LogType.INFO,
					"Updated Group of Player '" + pp.getUUID().toString() + "' from '" + current.getName()
							+ "' to '" + group.getName() + "'.");
			return true;
			
		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR,
					"Could not set Group of Player '" + pp.getUUID().toString() + "' from '" + current.getName()
							+ "' to '" + group.getName() + "'/SQL: " + e.getMessage());
			return true;
		}

	}

	private final PermGroup getGroupByResultSet(@Nonnull ResultSet rs) throws SQLException {

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

	private final PermPlayer getPlayerByResultSet(@Nonnull ResultSet rs) throws SQLException {

		UUID uuid = UUID.fromString(rs.getString(1));
		int groupID = rs.getInt(2);
		long expire = rs.getLong(3);

		return new PermPlayer(uuid, groupID, expire);
	}

	private final PermPermission getPermissionByResultSet(@Nonnull ResultSet rs) throws SQLException {

		String permission = rs.getString(1);
		int groupID = rs.getInt(2);
		int saveType = rs.getInt(3);

		return new PermPermission(permission, groupID, saveType);
	}

	public final void reload() {
		this.loadGroupsFromDatabase();
		this.loadPermissionsFromDatabase();
		this.loadPlayersFromDatabase();

		for (Player all : Bukkit.getOnlinePlayers()) {
			this.reloadPermissions(all);
		}

	}

	public final void loadPermissionsFromDatabase() {

		if (!NayolaPerms.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Not loading permissions from Database because no connection has been established.");
			return;
		}

		Core.getInstance().log(this.getClass(), LogType.INFO, "Loading Permissions from Database..");
		this.getPermissions().clear();

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + NayolaPerms.table_permissions + " WHERE saveType = ?");
			stmt.setInt(1, Core.getInstance().getSettingsManager().getSaveType().getID());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				PermPermission pp = this.getPermissionByResultSet(rs);

				if (pp != null) {
					this.getPermissions().add(pp);
				}

			}

			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Loaded " + this.getPermissions().size() + " Permissions from Database.");

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not load permissions from database/SQL: " + e.getMessage());
		}

	}

	public final boolean hasPermission(@Nonnull PermGroup group, @Nonnull String permission) {

		PermGroup least = this.getLeastPriorityGroupWithPermission(permission);

		if (least != null) {

//			Core.getInstance().log(getClass(), LogType.DEBUG, "Permission " + permission + " requires the group " + least.getName() + " (P: " + least.getPriority() + "). Entered group: " + group.getName() + "(P: " + group.getPriority() + ").");

			if (group.getPriority() < least.getPriority()) {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "Group " + least.toString() + " has permission "
//						+ permission + " at least priority. Entered group higher: NO");
				return false;
			} else {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "Group " + least.toString() + " has permission "
//						+ permission + " at least priority. Entered group higher: YES");
				return true;
			}
		} else {
//			Core.getInstance().log(getClass(), LogType.DEBUG,
//					"Permission " + permission + " is not saved in any saveType.");
			return false;
		}

	}

	public final PermGroup getLeastPriorityGroupWithPermission(@Nonnull String permission) {

		for (PermGroup pg : this.getGroupsInReverseOrder()) {

//			Core.getInstance().log(getClass(), LogType.DEBUG,
//					"Checking group " + pg.toString() + " for 'getLeastPriorityGroupWithPermission'..");

			if (pg.hasPermission(permission)) {
//				Core.getInstance().log(getClass(), LogType.DEBUG, "GROUP " + pg.getName() + " HAS PERMISSION " + permission);
				return pg;
			}

		}

		return null;
	}
	
	public final void reloadPermissionsForAllPlayers() {
		
		for(Player all : Bukkit.getOnlinePlayers()) {
			this.reloadPermissions(all);
		}
		
	}

	public final boolean reloadPermissions(@Nonnull Player p) {

		if(p == null) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Could not update permissions (Attachments) of entered player because it is null.");
			return false;
		}
		
		PermPlayer pp = this.getPlayer(p.getUniqueId());

		if (pp == null) {
			Core.getInstance().log(getClass(), LogType.WARNING, "Could not update permissions (Attachments) of player '" + p.getUniqueId().toString() + "' because the perm player object is null. Removing player for security reasons.");
			p.kickPlayer(CANT_UPDATE_PERM_ATTACHMENTS_KICKED);
			return false;
		}

		if (this.getAttachments().get(p.getUniqueId()) != null) {
			p.removeAttachment(this.getAttachments().get(p.getUniqueId()));
			this.getAttachments().remove(p.getUniqueId(), this.getAttachments().get(p.getUniqueId()));
		}

		PermissionAttachment attachment = p.addAttachment(NayolaPerms.getInstance());
		this.getAttachments().put(p.getUniqueId(), attachment);

		for (PermPermission permissions : this.getPermissionsFromAllGroups(pp.getGroup())) {
			attachment.setPermission(permissions.getPermission(), true);
		}

		SpigotMain.getInstance().getLocalScoreboard().refreshDefaultTeams();
		return true;
	}

	public final boolean addPermission(@Nonnull CommandSender cs, @Nonnull PermGroup group, @Nonnull String permission) {

		for(PermPermission pp : this.getPermissions()) {
			if(pp.getPermission().equalsIgnoreCase(permission)) {
				Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
						"command.nayolaperms.permission.add.error.permission-already-exists",
						Arrays.asList(group.getName(), permission, pp.getGroup().getDisplayName()), true);
				return false;
			}
		}
		
		if (this.hasPermission(group, permission)) {
			PermGroup lp = this.getLeastPriorityGroupWithPermission(permission);
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.add.error.already-has-permission",
					Arrays.asList(group.getName(), permission, lp.getName()), true);
			return false;
		}

		PermPermission pp = new PermPermission(permission, group.getID(),
				Core.getInstance().getSettingsManager().getSaveType().getID());

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("INSERT INTO " + NayolaPerms.table_permissions
							+ " (permission, groupID, saveType) VALUES (?, ?, ?);");
			stmt.setString(1, pp.getPermission());
			stmt.setInt(2, pp.getGroupID());
			stmt.setInt(3, pp.getServerID());

			stmt.executeUpdate();

			if (NayolaPerms.getInstance().isCachingEnabled()) {
				this.getPermissions().add(pp);
				this.reloadPermissionsForAllPlayers();
			}

			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.add.success",
					Arrays.asList(group.getName(), permission), true);
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add permission '" + permission + "' to group '"
					+ group.toString() + "'/SQL: " + e.getMessage());
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.add.error.sql",
					Arrays.asList(group.getName(), permission), true);
			return false;
		}

	}
	
	public final boolean removePermission(@Nonnull CommandSender cs, @Nonnull PermGroup group, @Nonnull String permission) {

		if (!this.hasPermission(group, permission)) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.remove.error.does-not-have-permission",
					Arrays.asList(group.getDisplayName(), permission), true);
			return false;
		}
		
		PermGroup belongs = this.getLeastPriorityGroupWithPermission(permission);
		
		if(group.getID() != belongs.getID()) {
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.remove.error.permission-does-not-belong-to-group",
					Arrays.asList(group.getDisplayName(), permission, belongs.getDisplayName()), true);
			return false;
		}

		PermPermission pp = group.getPermission(permission);

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("DELETE FROM " + NayolaPerms.table_permissions
							+ " WHERE permission = ? AND groupID = ? AND saveType = ?");
			stmt.setString(1, pp.getPermission());
			stmt.setInt(2, pp.getGroupID());
			stmt.setInt(3, pp.getServerID());

			int i = stmt.executeUpdate();
			
			if(i <= 0) {
				cs.sendMessage(PERMISSION_REMOVE_RESULT_SET_EMPTY);
				return false;
			}

			if (NayolaPerms.getInstance().isCachingEnabled()) {
				this.getPermissions().remove(pp);
				this.reloadPermissionsForAllPlayers();
			}

			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.remove.success",
					Arrays.asList(group.getName(), permission), true);
			return true;

		} catch (SQLException e) {
			Core.getInstance().log(getClass(), LogType.ERROR, "Could not add permission '" + permission + "' to group '"
					+ group.toString() + "'/SQL: " + e.getMessage());
			Core.getInstance().getLanguageManager().sendMessage(cs, NayolaPerms.getInstance().getPluginName(),
					"command.nayolaperms.permission.remove.error.sql",
					Arrays.asList(group.getName(), permission), true);
			return false;
		}

	}

	public final void loadPlayersFromDatabase() {

		if (!NayolaPerms.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Not loading players from Database because no connection has been established.");
			return;
		}

		if (!Core.getInstance().getSettingsManager().isSetting(NayolaPerms.getInstance().getPluginName(),
				"load-players-on-server-start")) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Not loading players from Database because loading them on server start has been disabled.");
			return;
		}

		Core.getInstance().log(this.getClass(), LogType.INFO, "Loading Players from Database..");
		this.getPlayers().clear();

		try {

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection()
					.prepareStatement("SELECT * FROM " + NayolaPerms.table_players);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {

				PermPlayer pp = this.getPlayerByResultSet(rs);

				if (pp != null) {
					this.getPlayers().add(pp);
				}

			}

			Core.getInstance().log(this.getClass(), LogType.INFO,
					"Loaded " + this.getPlayers().size() + " Players from Database.");

		} catch (SQLException e) {
			Core.getInstance().log(this.getClass(), LogType.ERROR,
					"Could not load players from database/SQL: " + e.getMessage());
		}

	}

	public final void loadGroupsFromDatabase() {

		if (!NayolaPerms.getInstance().getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG,
					"Not loading groups from Database because no connection has been established.");
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
		groups.add(new PermGroup("Leadership", Material.DIAMOND_PICKAXE, false, 480));
		groups.add(new PermGroup("Administrator", Material.DIAMOND_CHESTPLATE, false, 470));
		groups.add(new PermGroup("Developer", Material.LAVA_BUCKET, false, 460));
		groups.add(new PermGroup("Content", Material.WRITABLE_BOOK, false, 450));
		groups.add(new PermGroup("Builder", Material.GOLDEN_AXE, false, 440));
		groups.add(new PermGroup("Moderator", Material.GOLDEN_SWORD, false, 430));
		groups.add(new PermGroup("Supporter", Material.STONE_SWORD, false, 420));
		groups.add(new PermGroup("Trainee", Material.WOODEN_SHOVEL, false, 410));
		// Special
		groups.add(new PermGroup("VIP", Material.SHULKER_SHELL, false, 310));
		// Premium
		groups.add(new PermGroup("Emerald", Material.EMERALD, false, 230));
		groups.add(new PermGroup("Diamond", Material.DIAMOND, false, 220));
		groups.add(new PermGroup("Gold", Material.RAW_GOLD, false, 210));
		// User
		groups.add(new PermGroup("Iron", Material.RAW_IRON, false, 130));
		groups.add(new PermGroup("Coal", Material.COAL, false, 120));
		groups.add(new PermGroup("Stone", Material.STONE, true, 110));

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

			PreparedStatement stmt = NayolaPerms.getInstance().getMySQL().getConnection().prepareStatement(
					"INSERT INTO " + NayolaPerms.table_groups
							+ " (name, icon, isDefault, priority) VALUES (?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS);
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

//	public final Collection<PermPermission> getPermissions(@Nonnull PermGroup group) {
//
//		Collection<PermPermission> permList = new ArrayList<>();
//
//		for (PermPermission permission : this.getPermissions()) {
//			
//			PermGroup pg = permission.getGroup();
//			int permGroupPriority = pg.getPriority();
//			int groupPriority = permGroupPriority;
//			
//			if (permGroupPriority <= groupPriority) {
//				permList.add(permission);
//			}
//		}
//
//		return permList;
//	}

	public final Collection<PermPermission> getPermissions(@Nonnull PermGroup group) {

		Collection<PermPermission> permList = new ArrayList<>();

		for (PermPermission permission : this.getPermissions()) {
			if (permission.getGroup().getID() == group.getID()) {
				permList.add(permission);
			}
		}

		return permList;
	}

	public final Collection<PermPermission> getPermissionsFromAllGroups(@Nonnull PermGroup group) {

		Collection<PermPermission> permList = new ArrayList<>();

		for (PermPermission permission : this.getPermissions()) {
			if (permission.getGroup().getPriority() <= group.getPriority()) {
				permList.add(permission);
			}
		}

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
