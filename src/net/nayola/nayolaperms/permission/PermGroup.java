package net.nayola.nayolaperms.permission;

import org.bukkit.Material;

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
	private Material icon;
	private String hexColor;
	private boolean isDefault;
	private int priority;

	public PermGroup(int id, String name, Material icon, String hexColor, boolean isDefault, int priority) {
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.hexColor = hexColor;
		this.isDefault = isDefault;
		this.priority = priority;
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

	public void setName(String name) {
		this.name = name;
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
