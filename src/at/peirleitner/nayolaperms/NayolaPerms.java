package at.peirleitner.nayolaperms;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import at.peirleitner.core.Core;
import at.peirleitner.core.manager.LanguageManager;
import at.peirleitner.core.util.LogType;
import at.peirleitner.core.util.database.CredentialsFile;
import at.peirleitner.core.util.database.MySQL;
import at.peirleitner.core.util.database.TableType;
import at.peirleitner.nayolaperms.command.CommandGroups;
import at.peirleitner.nayolaperms.command.CommandNayolaPerms;
import at.peirleitner.nayolaperms.command.CommandRank;
import at.peirleitner.nayolaperms.listener.AsyncPlayerPreLoginListener;
import at.peirleitner.nayolaperms.listener.PlayerJoinListener;
import at.peirleitner.nayolaperms.listener.PlayerQuitListener;
import at.peirleitner.nayolaperms.manager.PermissionManager;

public class NayolaPerms extends JavaPlugin {

	private static NayolaPerms instance;
	private File mysqlFile;
	private MySQL mysql;
	
	public static final String table_groups = "permissions_groups";
	public static final String table_players = "permissions_players";
	public static final String table_permissions = "permissions_permissions";
	
	private PermissionManager permissionManager;

	@Override
	public void onEnable() {


		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		instance = this;

		// Util
		this.registerSettings();
		this.registerMessages();
		
		this.mysqlFile = CredentialsFile.getCredentialsFile(this.getDescription().getName(),
				this.getDataFolder().getPath());
		this.mysql = new MySQL(this.getDescription().getName(), mysqlFile);
		this.createTables();

		// Manager
		this.permissionManager = new PermissionManager();

		// Commands
		new CommandNayolaPerms();
		new CommandRank();
		new CommandGroups();

		// Listener
		new AsyncPlayerPreLoginListener();
		new PlayerQuitListener();
		new PlayerJoinListener();

	}

	@Override
	public void onDisable() {
		if (this.mysql != null && this.mysql.isConnected()) {
			this.mysql.close();
		}
		
		for(Player all : Bukkit.getOnlinePlayers()) {
			
			PermissionAttachment attachment = this.getPermissionManager().getAttachments().get(all.getUniqueId());
			
			if(attachment == null) {
				continue;
			}
			
			all.removeAttachment(attachment);
			
		}
		
	}

	public static NayolaPerms getInstance() {
		return instance;
	}

	public MySQL getMySQL() {
		return this.mysql;
	}

	public final String getPluginName() {
		return this.getDescription().getName();
	}

	// Manager
	public final PermissionManager getPermissionManager() {
		return this.permissionManager;
	}
	
	private final void registerSettings() {

		// v1.0.0
		Core.getInstance().getSettingsManager().registerSetting(this.getPluginName(), "mysql.table-prefix", "permissions_");
		Core.getInstance().getSettingsManager().registerSetting(this.getPluginName(), "enable-caching", "true");
		Core.getInstance().getSettingsManager().registerSetting(this.getPluginName(), "load-players-on-server-start", "false");
		Core.getInstance().getSettingsManager().registerSetting(this.getPluginName(), "send-rank-info-on-join", "true");

	}
	
	public final boolean isCachingEnabled() {
		return Boolean.valueOf(Core.getInstance().getSettingsManager().getSetting(this.getPluginName(), "enable-caching"));
	}

	private void registerMessages() {

		LanguageManager languageManager = Core.getInstance().getLanguageManager();
		final String pluginName = this.getPluginName();

		// Main
		languageManager.registerNewMessage(pluginName, "prefix", "&9NayolaPerms> &f");
		languageManager.registerNewMessage(pluginName, "main.no-permission", "&cThis action requires the rank &7[&e{0}&7] &cor higher&7.");
		
		// Commands
		languageManager.registerNewMessage(pluginName, "command.rank.permanent", "&3Your current rank &e{0} &3never expires&7.");
		languageManager.registerNewMessage(pluginName, "command.rank.temporary", "&3Your current rank &e{0} &3expires on &e{1}&7.");
		languageManager.registerNewMessage(pluginName, "command.groups.list", "&3The following groups are available&8:");
		languageManager.registerNewMessage(pluginName, "command.groups.group", "&8- &e{0}");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.syntax", "&3Help for NayolaPerms version &e{0}&8:\n"
				+ "&3/nayolaperms\n"
				+ "  &egroup &6create &f<Name> <Icon> <Default> <Priority>\n" // 6
				+ "  &egroup &6set &f<Player> <Group> [Days]\n" // 4-5
				+ "  &egroup &6get &f<Player>\n" // 3
				+ "  &epermission &6get &f<Group>\n" // 3
				+ "  &epermission &6add &f<Group> <Permission>\n" // 4
				+ "  &epermission &6remove &f<Group> <Permission>\n" // 4
				+ "  &ereload \n" // 1
				+ "  &eloadDefaultGroups");  // 1
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.main.error.group-does-not-exist-id", "&cGroup with ID &e{0} &cdoes not exist.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.main.error.group-does-not-exist-name", "&cGroup with Name &e{0} &cdoes not exist.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.main.error.player-has-no-profile", "&cThe Player &e{0} &cdoes not have a permission profile.");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.get.success", "&7The Player &e{0} &7currently holds the group &e{1}&7.");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.set.success.sender", "&7Successfully changed the Group of &9{0} &7from &9{1} &7to &9{2}&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.set.success.error", "&cCould not change the Group of &e{0} &cfrom &e{1} &cto &e{2}&c, see console for details.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.set.success.target", "&9{0} &7changed your Group from &9{1} &7to &9{2}&7.");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.name-already-exists", "&cGroup with the Name '&e{0}&c' does already exist&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.diplayName-already-exists", "&cGroup with the DisplayName '&e{0}&c' does already exist&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.icon-already-exists", "&cGroup with the Icon '&e{0}&c' does already exist&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.hexColor-already-exists", "&cGroup with the HexColor '&e{0}&c' does already exist&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.priority-already-exists", "&cGroup with the Priority '&e{0}&c' does already exist&7.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.group.create.error.default-group-already-exists", "&cA default group '&e{0}&c' does already exist&7.");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.permission.get.success.no-permissions", "&7The Group &9{0} &7does not have any Permissions associated with them.");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.permission.get.success.pre-text", "&7The Group &9{0} &7has the following Permissions (&9{1}&7)&8:");
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.permission.get.success.permission", "&8- &e{0} &7(From Group &6{1}&7)");
		
		languageManager.registerNewMessage(pluginName, "command.nayolaperms.permission.add.error.already-has-permission", "&cThe group &e{0} &cdoes already have the permission &e{1} &cbased on group &e{2}&c.");
		
		// Listener
//		languageManager.registerNewMessage(pluginName, "listener.async-player-pre-login.error.no-database-connection", "Could not validate connection towards the permission database.");
		languageManager.registerNewMessage(pluginName, "listener.async-player-pre-login.error.could-not-create-player", "&cCould not create new player permission object; connection cancelled.");

		// GUIs

	}

	private void createTables() {
		
		if(this.getMySQL() == null || !this.getMySQL().isConnected()) {
			Core.getInstance().log(getClass(), LogType.DEBUG, "Did not attempt to create Tables because no Database Connection has been established.");
			return;
		}
		
		List<String> statements = new ArrayList<>();
		
		statements.add("CREATE TABLE IF NOT EXISTS " + table_groups + " ("
				+ "id INT AUTO_INCREMENT NOT NULL, "
				+ "name VARCHAR(100) NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'COBBLESTONE', "
				+ "isDefault BOOLEAN NOT NULL DEFAULT '0', "
				+ "priority INT NOT NULL DEFAULT '100', "
				+ "PRIMARY KEY (id, name));");
		
		statements.add("CREATE TABLE IF NOT EXISTS " + table_players + " ("
				+ "uuid CHAR(36) PRIMARY KEY NOT NULL, "
				+ "groupID INT NOT NULL, "
				+ "expire BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "FOREIGN KEY (groupID) REFERENCES " + table_groups + "(id)"
						+ ");");
		
		statements.add("CREATE TABLE IF NOT EXISTS " +  table_permissions + "("
				+ "permission VARCHAR(100) NOT NULL, "
				+ "groupID INT NOT NULL, "
				+ "saveType INT NOT NULL, "
				+ "PRIMARY KEY (permission, groupID, saveType), "
				+ "FOREIGN KEY (groupID) REFERENCES " + table_groups + "(id), "
				+ "FOREIGN KEY (saveType) REFERENCES " + TableType.SAVE_TYPE.getTableName(true) + "(id)"
				+ ");");
		
		for(String s : statements) {
			
			try {
				
				PreparedStatement stmt = this.getMySQL().getConnection().prepareStatement(s);
				stmt.execute();
				
			} catch (SQLException e) {
				Core.getInstance().log(this.getClass(), LogType.ERROR, "Could not create Table/SQL: " + e.getMessage());
				return;
			}
			
		}
		
	}

}
