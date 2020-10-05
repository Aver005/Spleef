package kiviuly.Spleef;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener
{
	private Main main;
	public Events(Main main) {this.main = main;}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e)
	{
		String title = e.getView().getTitle();
		Player p = (Player) e.getWhoClicked();
		
		if (title.equals(main.titleName))
		{
			e.setCancelled(true);
			if (!main.getPlayerStatus(p).equals(Status.NotInGame)) {p.closeInventory(); return;}
			ItemStack item = e.getCurrentItem();
			if (!item.getType().equals(Material.WOOL)) {return;}
			
			if (item.getDurability() == 5)
			{
				p.closeInventory();
				Arena arena = main.getArenaByDisplayName(item.getItemMeta().getDisplayName());
				if (arena == null) {p.sendMessage("§cОшибка. Данная арена не найдена."); main.OpenMainMenu(p); return;}
				
				if (arena.addPlayer(p)) {} else {p.sendMessage("§eДанная арена заполнена. Попробуйте другую :("); ; main.OpenMainMenu(p);}
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e)
	{
		Player p = e.getPlayer();
		Arena arena = main.getPlayerArena(p);
		if (arena == null) {return;}
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (arena.isOnlyAllowedBlocks() && !arena.getAllowedToBreakBlocks().contains(b.getType().name())) {e.setCancelled(true); return;}
		arena.addModifedBlock(b.getLocation(), b.getType());
		e.setDropItems(false);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame)) {return;}
		Arena arena = main.getPlayerArena(p);
		arena.removePlayer(p);
	}
	
	@EventHandler
	public void onPlayerChat(PlayerChatEvent e)
	{
		Player p = e.getPlayer();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame) || status.equals(Status.Waiting)) {return;}
		e.setCancelled(true);
		p.sendMessage("§eЧат во время игры недоступен.");
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e)
	{
		Player p = e.getPlayer();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame)) {return;}
		if (e.getMessage().contains("leave")) {return;}
		e.setCancelled(true);
		p.sendMessage("§eКоманды запрещены во время игры.");
	}
	
	@EventHandler
	public void onPlayerGetDamage(EntityDamageEvent e)
	{
		if (!e.getEntity().getType().equals(EntityType.PLAYER)) {return;}
		Player p = (Player) e.getEntity();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (status.equals(Status.Spectating) || status.equals(Status.Waiting) || status.equals(Status.EndTheGame)) {e.setCancelled(true); return;}
		if (e.getCause().equals(DamageCause.FALL) || e.getCause().equals(DamageCause.FIRE) || e.getCause().equals(DamageCause.FIRE_TICK)) 
		{e.setCancelled(true); return;}
		if (e.getDamage() >= p.getHealth()) {e.setDamage(0); arena.addDeadPlayer(p);}
	}
	
	@EventHandler
	public void onPlayerDamagePlayer(EntityDamageByEntityEvent e)
	{
		if (!e.getEntity().getType().equals(EntityType.PLAYER)) {return;}
		Player p = (Player) e.getEntity();
		Status status = main.getPlayerStatus(p);
		if (status.equals(Status.NotInGame)) {return;}
		Arena arena = main.getPlayerArena(p);
		if (arena == null) {return;}
		if (status.equals(Status.Spectating)) {e.setCancelled(true); return;}
		if (status.equals(Status.Waiting))
		{
			if (!e.getDamager().getType().equals(EntityType.PLAYER)) 
			{
				e.setDamage(0);
				return;
			}
			
			Player d = (Player) e.getDamager();
			double dmg = e.getDamage();
			e.setDamage(0);
			arena.addLobbyDamage(d.getUniqueId(), dmg);
			return;
		}
		
		if (!arena.isPVPEnabled())
		{
			e.setCancelled(true);
			return;
		}
	}
}
