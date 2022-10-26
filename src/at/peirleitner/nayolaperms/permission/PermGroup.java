package at.peirleitner.nayolaperms.permission;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.bukkit.Material;

import at.peirleitner.core.Core;
import at.peirleitner.core.util.user.Rank;
import at.peirleitner.nayolaperms.NayolaPerms;
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
	private Material icon;
	private boolean isDefault;
	private int priority;

	public PermGroup() {
	}

	public PermGroup(@Nonnull String name, @Nonnull Material icon, @Nonnull boolean isDefault, @Nonnull int priority) {
		this.name = name;
		this.icon = icon;
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
	
	public String getDisplayName() {
		return ChatColor.of(this.getRank().getColor()) + this.getName();
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

	public final Collection<PermPermission> getPermissions() {
		return NayolaPerms.getInstance().getPermissionManager().getPermissions(this);
	}
	
	public final PermPermission getPermission(@Nonnull String permission) {
		return this.getPermissions().stream().filter(perm -> perm.getPermission().equalsIgnoreCase(permission)).findAny().orElse(null);
	}
	
	public final boolean hasPermission(@Nonnull String permission) {
		return this.getPermission(permission) == null ? false : true;
	}
	
	public final Collection<PermPlayer> getPlayers() {
		
		Collection<PermPlayer> players = new ArrayList<>();
		
		for(PermPlayer pp : NayolaPerms.getInstance().getPermissionManager().getPlayers()) {
			if(pp.getGroupID() == this.getID()) {
				players.add(pp);
			}
		}
		
		return players;
	}
	
	/**
	 * 
	 * @return {@link Rank} equivalent of this group.
	 * @since 1.0.0
	 * @author Markus Peirleitner (Rengobli)
	 * @apiNote This does only work if {@link Rank#getPriority()} and {@link #getPriority()} are equal.
 	 */
	public final Rank getRank() {
		return Core.getInstance().getRankByPriority(this.getPriority());
	}
	
	@Override
	public String toString() {
		return "PermGroup[id=" + this.getID() + ",name=" + this.getName() + ",icon=" + this.getIcon().toString() + ",isDefault=" + this.isDefault() + ",priority=" + this.getPriority() + "]";
	}

}
