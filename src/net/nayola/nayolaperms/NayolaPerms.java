package net.nayola.nayolaperms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
import net.nayola.nayolaperms.listener.PlayerJoinListener;
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

		// Listener
		new PlayerJoinListener();
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

		// Commands

		// Listener

		// Inventories

	}

	private void createTables() {
		
		// Groups
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_groups + " ("
				+ "id INT AUTO_INCREMENT NOT NULL, "
				+ "name VARCHAR(100) PRIMARY KEY NOT NULL, "
				+ "icon VARCHAR(100) NOT NULL DEFAULT 'COBBLESTONE', "
				+ "hexColor CHAR(6) NOT NULL DEFAULT 'ffffff', "
				+ "isDefault BOOLEAN NOT NULL DEFAULT '0', "
				+ "priority INT NOT NULL DEFAULT '100'"
				+ ");");
		
		// Players
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_players + " ("
				+ "uuid CHAR(36) PRIMARY KEY NOT NULL, "
				+ "group int NOT NULL, "
				+ "expire BIGINT(255) NOT NULL DEFAULT '-1', "
				+ "FOREIGN KEY(group) REFERENCES (" + table_groups + ".id)"
						+ ");");
		
		// Permissions
		this.getMySQL().prepareStatement("CREATE TABLE IF NOT EXISTS " + table_permissions + "("
				+ "permission VARCHAR(100) NOT NULL, "
				+ "group INT NOT NULL, "
				+ "server INT NOT NULL, "
				+ "PRIMARY KEY (permission, group, server), "
				+ "FOREIGN KEY(group) REFERENCES (" + table_groups + ".id)"
				+ ");");
		
	}

}
