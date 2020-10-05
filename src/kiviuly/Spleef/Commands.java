package kiviuly.Spleef;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor
{
	private Main main;
	public Commands(Main main) {this.main = main;}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player) sender;
			if (args.length == 0) {main.OpenMainMenu(p); return true;}
			String sub = args[0].toLowerCase();
			
			if (sub.equals("help")) {SendHelpMessage(p); return true;}
			if (sub.equals("join")) 
			{
				if (args.length == 1) {main.OpenMainMenu(p); return true;}
				
			}
			if (sub.equals("leave")) 
			{
				Arena arena = main.getPlayerArena(p);
				if (arena == null) {p.sendMessage("§eВы не в игре."); return true;}
				arena.removePlayer(p);
			}
			
			if (!p.hasPermission("spleef.admin")) {SM(p, "§cНеизвестная подкоманда."); return true;}
			if (args.length < 2) {return true;}
			String name = args[1].toUpperCase();
			 
			if (sub.equals("create"))
			{
				if (main.isArena(name)) {SM(p, "§cДанная арена сущесвует."); return true;}
				Arena arena = new Arena(name, p.getUniqueId());
				main.addArena(name, arena);
				p.sendMessage("§2Арена §b"+name+"§2 успешно создана.");
				return true;
			}
			
			if (sub.equals("enable") || sub.equals("on"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				arena.setEnabled(true);
				p.sendMessage("§2Арена §b"+name+"§2 §lВКЛЮЧЕНА.");
				return true;
			}
			
			if (sub.equals("disable") || sub.equals("off"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				arena.setEnabled(false);
				p.sendMessage("§2Арена §b"+name+"§2 §e§lВЫКЛЮЧЕНА.");
				return true;
			}
			
			if (sub.equals("addspawn") || sub.equals("setspawn"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				arena.addSpawn(p.getLocation());
				p.sendMessage("§2Спавн для игроков был добавлен на арену §b"+name+"§2.");
				return true;
			}
			
			if (sub.equals("setlobby"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				arena.setLobbyLocation(p.getLocation());
				p.sendMessage("§2Лобби для арены §b"+name+"§2 было установлено.");
				return true;
			}
			
			if (sub.equals("setspectator") || sub.equals("setwatch"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				arena.setSpectateLocation(p.getLocation());
				p.sendMessage("§2Точка наблюдения для арены §b"+name+"§2 была установлена.");
				return true;
			}
			
			if (args.length < 3) {return true;}
			
			if (sub.equals("setname"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				String dName = "";
				for(int i = 2; i < args.length; i++) {dName+=args[i]+" ";}
				dName = ChatColor.translateAlternateColorCodes('&', dName.substring(0, dName.length()-1));
				arena.setName(dName);
				p.sendMessage("§2Имя арены §b"+name+"§2 изменено на "+dName+"§2.");
				return true;
			}
			
			if (sub.equals("setdesc") || sub.equals("setdescription"))
			{
				if (!main.isArena(name)) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				Arena arena = main.getArena(name);
				if (arena == null) {SM(p, "§cДанная арена НЕ сущесвует."); return true;}
				String dName = "";
				for(int i = 2; i < args.length; i++) {dName+=args[i]+" ";}
				dName = ChatColor.translateAlternateColorCodes('&', dName.substring(0, dName.length()-1));
				arena.setDescription(dName);
				p.sendMessage("§2Описание арены §b"+name+"§2 изменено на "+dName+"§2.");
				return true;
			}
		}
		
		return false;
	}

	public void SendHelpMessage(Player p) 
	{
		String msg = 
	          "§7╔═════ §2Команды мини-режима §7═════►" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §b/spf §7——— §2Открыть меню мини-игры" + "\n"
	        + "§7‖  §b/spf join §7——— §2Вступить в случайный матч" + "\n"
	        + "§7‖  §b/spf join [Арена] §7——— §2Вступить в открытый матч" + "\n"
	        + "§7‖  §b/spf leave §7——— §2Выйти из матча" + "\n"
	        + "§7‖  §b/spf stats §7——— §2Посмотреть статистику" + "\n"
	        + "§7‖  §b/spf stats [Игрок] §7——— §2Посмотреть статистику игрока" + "\n"
	        + "§7‖  §b/spf info §7——— §2Описание и правила режима" + "\n"
	        + "§7‖" + "\n"
	        + "§7╚═══════════════════════════════►";
	
	    if (p.hasPermission("spleef.admin"))
	    {
	        msg +=
	          "\n\n"
	        + "§7╔═════ §eКоманды админа §7═════►" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/spf reload §7——— §eПерезагрузить настройки плагина" + "\n"
	        + "§7‖  §c/spf save §7——— §eСохранить настройки плагина" + "\n"
	        + "§7‖  §c/spf list §7——— §eСписок арен" + "\n"
	        + "§7‖  §c/spf stop <ArenaID/all> §7——— §eОстановить игру" + "\n"
	        + "§7‖  §c/spf start <ArenaID/all> §7——— §eЗапустить игру" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/spf create <ArenaID> §7——— §eСоздать новую арену" + "\n"
	        + "§7‖  §c/spf setlobby <ArenaID> §7——— §eПоставить точку лобби" + "\n"
	        + "§7‖  §c/spf addspawn <ArenaID> §7——— §eПоставить точку появления игроков" + "\n"
	        + "§7‖  §c/spf setminpl <ArenaID> <MinPlCount> §7——— §eУстановить мин. кол-во игроков" + "\n"
	        + "§7‖  §c/spf setmaxpl <ArenaID> <MaxPlCount> §7——— §eУстановить макс. кол-во игроков" + "\n"
	        + "§7‖  §c/spf addblock <ArenaID> §7——— §eДобавить блок для ломания" + "\n"
	        + "§7‖  §c/spf additem <ArenaID> §7——— §eДобавить стартовый предмет" + "\n"
	        + "§7‖  §c/spf sety <ArenaID> <Y> §7——— §eУстановить смертную высоту" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §eДля получения инструкции по созданию и настройке арены," + "\n"
	        + "§7‖  §eНапишите команду §c/bw review" + "\n"
	        + "§7‖" + "\n"
	        + "§7‖  §c/spf enable <ArenaID> §7——— §eВключить арену" + "\n"
	        + "§7‖  §c/spf disable <ArenaID> §7——— §eВыключить арену" + "\n"
	        + "§7‖  §c/spf check <ArenaID> §7——— §eПроверить арену на настройку" + "\n"
	        + "§7‖  §c/spf review §7——— §eПолучить инструкцию" + "\n"
	        + "§7‖" + "\n"
	        + "§7╚═══════════════════════════════►";
	    }
	
	    p.sendMessage(msg);
	}
	
	public void SM(Player p, String msg) {p.sendMessage(msg);}
	public void SM(String msg, Player p) {p.sendMessage(msg);}
}