package net.nayola.nayolaperms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.nayola.core.NayolaCore;
import net.nayola.core.manager.language.LanguageManagerSpigot;
import net.nayola.core.util.EconomyType;
import net.nayola.core.util.LogType;
import net.nayola.core.util.MySQL;
import net.nayola.core.util.Rank;
import net.nayola.nayolaperms.command.CommandGroups;
import net.nayola.nayolaperms.command.CommandNayolaPerms;
import net.nayola.nayolaperms.command.CommandRank;
import net.nayola.nayolaperms.listener.AsyncPlayerPreLoginListener;
import net.nayola.nayolaperms.listener.PlayerQuitListener;
import net.nayola.nayolaperms.manager.PermissionManager;

public class NayolaPerms extends JavaPlugin {

	private static NayolaPerms instance;
	private MySQL mysql;
	private File credentials = new File(this.getDataFolder() + "/mysql.yml");

	private Collection<UUID> inventoryClick;
	
	public static final String table_groups = "perms_groups";
	public static final String table_players = "perms_players";
	public static final String table_permissions = "perms_permissions";
	
	private PermissionManager permissionManager;

	@Override
	public void onEnable() {

		if (!Bukkit.getPluginManager().isPluginEnabled("NayolaCoreSpigot")) {
			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.RED + "NayolaCoreSpigot is not loaded! Disabling " + this.getDescription().getName() + " Plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		instance = this;
		this.loadConfig();
		this.registerMessages();

		inventoryClick = new ArrayList<>();

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}

		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(credentials);

		if (!credentials.exists()) {
			try {
				NayolaCore.getInstance().logSpigot(this, LogType.DEBUG,
						"MySQL file does not exist! Attempting to create..");

				credentials.createNewFile();
				cfg.set("host", "localhost");
				cfg.set("database", "template");
				cfg.set("port", 3306);
				cfg.set("username", "root");
				cfg.set("password", "passy");
				cfg.save(credentials);

				NayolaCore.getInstance().logSpigot(this, LogType.DEBUG, "Successfully created MySQL file");
			} catch (IOException e) {
				NayolaCore.getInstance().logSpigot(this, LogType.ERROR,
						"Could not create credentials file for MySQL: " + e.getMessage());
				return;
			}
		}

		mysql = new MySQL(credentials, cfg.getString("host"), cfg.getString("database"), cfg.getInt("port"),
				cfg.getString("username"), cfg.getString("password"));
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

	}

	@Override
	public void onDisable() {
		this.getMySQL().close();
	}

	public static NayolaPerms getInstance() {
		return instance;
	}

	public MySQL getMySQL() {
		return this.mysql;
	}

	private void loadConfig() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}

	public Collection<UUID> getInventoryClick() {
		return this.inventoryClick;
	}

	public EconomyType getEconomyType() {
		return EconomyType.valueOf(this.getConfig().getString("economy-type").toUpperCase());
	}

	// Manager
	public final PermissionManager getPermissionManager() {
		return this.permissionManager;
	}

	// Util
	public Rank getRequiredRank(String key) {

		try {

			Rank rank = Rank.valueOf(this.getConfig().getString("requiredRank." + key));
			return rank;

		} catch (IllegalArgumentException ex) {
			NayolaCore.getInstance().logSpigot(this, LogType.WARN,
					"Could not get requiredRank for key " + key + ": Returned default value (OWNER).");
			return Rank.OWNER;
		}

	}

	private void registerMessages() {

		LanguageManagerSpigot languageManager = NayolaCore.getInstance().getLanguageManagerSpigot();

		// Main
		languageManager.registerNewMessage(this, "main.no-permission", "&cThis action requires the rank &7[&e{0}&7] &cor higher&7.");
		
		// Commands
		languageManager.registerNewMessage(this, "command.rank.permanent", "&3Your current rank &e{0} &3never expires&7.");
		languageManager.registerNewMessage(this, "command.rank.temporary", "&3Your current rank &e{0} &3expires on &e{1}&7.");
		languageManager.registerNewMessage(this, "command.groups.list", "&3The following groups are available&8:");
		languageManager.registerNewMessage(this, "command.groups.group", "&8- &e{0}");
		languageManager.registerNewMessage(this, "command.nayolaperms.syntax", "&3Help for NayolaPerms version &e{0}&8:\n"
				+ "&3/nayolaperms\n"
				+ "  &egroup &6create &f<Name> <DisplayName> <Icon> <Color> <Default> <Priority>\n" // 8
				+ "  &egroup &6set &f<Player> <Group> [Days]\n" // 4-5
				+ "  &egroup &6get &f<Player>\n" // 3
				+ "  &epermission &6get &f<Group>\n" // 3
				+ "  &epermission &6add &f<Group> <Permission>\n" // 4
				+ "  &epermission &6remove &f<Group> <Permission>\n" // 4
				+ "  &eplayer &6get &f<Player>\n" // 3
				+ "  &ereload"); 
		languageManager.registerNewMessage(this, "command.nayolaperms.group.create.error.name-already-exists", "&cGroup with the name '&e{0}&c' does already exist&7.");

		// Listener
		languageManager.registerNewMessage(this, "listener.join.error.could-not-create-player", "&cCould not create new player permission object; connection cancelled.");

		// Inventories

	}

	private void createTables() {
		
		// Groups
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_groups + " ("
				+ "id INT AUTO_INCREMENT NOT NULL, "
				+ "name VARCHAR(100) NOT NULL, "
				+ "displayName VARCHAR(100) NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'COBBLESTONE', "
				+ "hexColor CHAR(6) NOT NULL DEFAULT 'ffffff', "
				+ "isDefault BOOLEAN NOT NULL DEFAULT '0', "
				+ "priority INT NOT NULL DEFAULT '100', "
				+ "PRIMARY KEY (id, name));");
		
		// Players
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_players + " ("
				+ "uuid CHAR(36) PRIMARY KEY NOT NULL, "
				+ "groupID INT NOT NULL, "
				+ "expire BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "FOREIGN KEY (groupID) REFERENCES " + table_groups + "(id)"
						+ ");");
		
		// Permissions
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_permissions + "("
				+ "permission VARCHAR(100) NOT NULL, "
				+ "groupID INT NOT NULL, "
				+ "server INT NOT NULL, "
				+ "PRIMARY KEY (permission, groupID, server), "
				+ "FOREIGN KEY (groupID) REFERENCES " + table_groups + "(id)"
				+ ");");
		
	}

}
