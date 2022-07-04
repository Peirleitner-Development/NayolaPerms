package net.nayola.nayolaperms.permission;

import org.bukkit.Material;

import net.md_5.bungee.api.ChatColor;

/**
 * This class represents a permission group.
 * 
 * @since 1.0.0
 * @author Markus Peirleitner (Rengobli)
 *
 */
public class PermGroup {

	private int id;
	private String name;
	private String displayName;
	private Material icon;
	private String hexColor;
	private boolean isDefault;
	private int priority;

	public PermGroup() {
	}

	public final int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public final String getName() {
		return name;
	}

	public final String getColoredName() {
		return this.getChatColor() + this.getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public final String getColoredDisplayName() {
		return this.getChatColor() + this.getDisplayName();
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public final Material getIcon() {
		return icon;
	}

	public void setIcon(Material icon) {
		this.icon = icon;
	}

	public final String getHexColor() {
		return hexColor;
	}

	public void setHexColor(String hexColor) {
		this.hexColor = hexColor;
	}

	public final ChatColor getChatColor() {
		return ChatColor.of("#" + this.getHexColor());
	}

	public final boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public final int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
