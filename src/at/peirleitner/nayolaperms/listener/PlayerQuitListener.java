package at.peirleitner.nayolaperms.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import at.peirleitner.nayolaperms.NayolaPerms;
import at.peirleitner.nayolaperms.permission.PermPlayer;

public class PlayerQuitListener implements Listener {

	public PlayerQuitListener() {
		NayolaPerms.getInstance().getServer().getPluginManager().registerEvents(this, NayolaPerms.getInstance());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {

		Player p = e.getPlayer();

		PermPlayer pp = NayolaPerms.getInstance().getPermissionManager().getPlayerFromCache(p.getUniqueId());

		if (pp != null) {
			NayolaPerms.getInstance().getPermissionManager().getCachedPlayers().remove(pp);
		}
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				if(NayolaPerms.getInstance().getPermissionManager().getAttachments().containsKey(p.getUniqueId())) {
					PermissionAttachment pa = NayolaPerms.getInstance().getPermissionManager().getAttachments().get(p.getUniqueId());
					p.removeAttachment(pa);
					NayolaPerms.getInstance().getPermissionManager().getAttachments().remove(p.getUniqueId(), pa);
				}
				
			}
		}.runTaskLater(NayolaPerms.getInstance(), 20L);

	}

}
