package kiviuly.Spleef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import net.minecraft.server.v1_12_R1.ItemArmor;

public class Main extends JavaPlugin
{
	public static Main main;
	private HashMap<String, Arena> arenasByName = new HashMap<>();
	private HashMap<UUID, Arena> playersArenas = new HashMap<>();
	
	public String titleName = "§8§lSpleef";
	
	@Override
	public void onEnable()
	{
		main = this;
		List<String> aliases = new ArrayList<String>();
		aliases.add("sp"); aliases.add("spf");
		getCommand("spleef").setExecutor(new Commands(this));
		getServer().getPluginManager().registerEvents(new Events(this), this);
		
		File file = new File(getDataFolder()+File.separator);
		if (!file.exists()) {file.mkdir();}
		file = new File(getDataFolder()+File.separator+"Arenas");
		if (!file.exists()) {file.mkdir();}
		for(File f : file.listFiles()) {loadArena(f);}
	}
	
	@Override
	public void onDisable() 
	{
		for(String name : arenasByName.keySet())
		{
			Arena arena = arenasByName.get(name);
			File file = new File(getDataFolder()+File.separator);
			if (!file.exists()) {file.mkdir();}
			file = new File(getDataFolder()+File.separator+"Arenas");
			if (!file.exists()) {file.mkdir();}
			file = new File(getDataFolder()+File.separator+"Arenas"+File.separator+name+".yml");
			saveArena(file, arena);
		}
	}
	
	public Arena getPlayerArena(Player p)
	{
		UUID id = p.getUniqueId();
		return playersArenas.getOrDefault(id, null);
	}
	
	public void setPlayerArena(Player p, Arena arena)
	{
		UUID id = p.getUniqueId();
		if (arena == null) {playersArenas.remove(id); return;}
		playersArenas.put(id, arena);
	}
	
	public Arena getArena(String name)
	{
		return arenasByName.getOrDefault(name, null);
	}
	
	public Status getPlayerStatus(Player p)
	{
		Arena arena = getPlayerArena(p);
		if (arena == null) {return Status.NotInGame;}
		if (arena.isPlaying(p)) {return Status.Playing;}
		if (arena.isWaiting(p)) {return Status.Waiting;}
		if (arena.isSpectator(p)) {return Status.Spectating;}
		if (arena.getStatus().equals(ArenaStatus.Ending)) {return Status.EndTheGame;}
		playersArenas.remove(p.getUniqueId());
		return Status.NotInGame;
	}

	public HashMap<String, Arena> getArenasByName()
	{
		return arenasByName;
	}

	public void setArenasByName(HashMap<String, Arena> arenasByName)
	{
		this.arenasByName = arenasByName;
	}

	public HashMap<UUID, Arena> getPlayersArenas()
	{
		return playersArenas;
	}

	public void setPlayersArenas(HashMap<UUID, Arena> playersArenas)
	{
		this.playersArenas = playersArenas;
	}
	
	public Inventory fillFreeSlots(Inventory inv, short data, String type)
	{
		for(int i = 0; i < inv.getSize(); i++) 
		{
			if (inv.getItem(i) != null) {continue;}
			
			if (type.equals("WALLS") && (i > 9 && i < 44 && i % 9 != 0 && (i+1) % 9 != 0)) {continue;}
			ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(" ");
			item.setItemMeta(meta);
			inv.setItem(i,item);
		}
		return inv;
	}

	public void OpenMainMenu(Player p)
	{
		Inventory inv = Bukkit.createInventory(null, 54, titleName);
		inv = fillFreeSlots(inv, (short) 15, "WALLS");
		ArrayList<Arena> startedArenas = new ArrayList<>();
		for(String s : arenasByName.keySet())
		{
			Arena arena = arenasByName.get(s);
			if (!arena.isEnabled()) {continue;}
			if (arena.isStarted()) {startedArenas.add(arena); continue;}
			ItemStack is = new ItemBuilder(Material.WOOL)
				.damage((short)5).lore("")
				.lore("§fÈãðîêîâ: §2"+arena.getPlayersInLobby().size()+" / "+arena.getMaxPlayers())
				.lore("§fÎïèñàíèå:")
				.lore("§e"+arena.getDescription())
				.lore("").lore("§2§lÍàæìèòå, §2÷òîáû âîéòè")
				.displayname(arena.getName()).build();
			
			inv.addItem(is);
		}
		p.openInventory(inv);
	}
	
	public static String randomString(int targetStringLength) 
	{
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'
	    Random random = new Random();
	 
	    String generatedString = random.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(targetStringLength)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	 
	    return generatedString;
	}
	
	public int randomInt(int min, int max) 
	{
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public boolean isInt(String s)
	{
	    try {Integer.parseInt(s); return true;} 
	    catch (NumberFormatException ex) {return false;}
	}

	public void addArena(String name, Arena arena)
	{
		if (isArena(name)) {return;}
		arenasByName.put(name, arena);
	}

	public boolean isArena(String name)
	{
		return arenasByName.containsKey(name);
	}

	public Arena getArenaByDisplayName(String name)
	{
		for(String s : arenasByName.keySet())
		{
			Arena arena = arenasByName.get(s);
			String realName = ChatColor.translateAlternateColorCodes('&', arena.getName());
			name = ChatColor.translateAlternateColorCodes('&', name);
			if (realName.equals(name)) {return arena;}
		}
		
		return null;
	}
	
	public boolean saveArena(File file, Arena arena)
	{
		if (!file.exists()) {try {file.createNewFile();} catch (IOException e) {e.printStackTrace(); return false;}}
		if (arena == null) {return false;}
		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
		conf.set("ID", arena.getID());
		conf.set("Name", arena.getName());
		conf.set("Description", arena.getDescription());
		conf.set("MinPlayers", arena.getMinPlayers());
		conf.set("MaxPlayers", arena.getMaxPlayers());
		conf.set("Enabled", arena.isEnabled());
		conf.set("StartedItems", arena.getStartedItems());
		conf.set("Spawns", arena.getSpawns());
		conf.set("AllowedBlocks", arena.getAllowedToBreakBlocks());
		conf.set("OnlyAllowedBlocks", arena.isOnlyAllowedBlocks());
		conf.set("Creator", arena.getCreator().toString());
		conf.set("BestPlayer", arena.getBestPlayer().toString());
		conf.set("LobbyLocation", arena.getLobbyLocation());
		conf.set("WatchPointLocation", arena.getSpectateLocation());
		conf.set("EnabledPVP", arena.isPVPEnabled());
		conf.set("WelcomeMessage", arena.getWelcomeMessage());
		try {conf.save(file);} catch (IOException e) {e.printStackTrace();}
		return true;
	}
	
	public boolean loadArena(File file)
	{
		if (!file.exists()) {return false;}
		String name = file.getName().replace(".yml", "");
		Arena arena = new Arena(name, null);
		FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
		arena.setID(conf.getString("ID", arena.getID()));
		arena.setName(conf.getString("Name", arena.getName()));
		arena.setDescription(conf.getString("Description", arena.getDescription()));
		arena.setMinPlayers(conf.getInt("MinPlayers", arena.getMinPlayers()));
		arena.setMaxPlayers(conf.getInt("MaxPlayers", arena.getMaxPlayers()));
		arena.setEnabled(conf.getBoolean("Enabled", arena.isEnabled()));
		arena.setStartedItems((ArrayList<ItemStack>) conf.get("StartedItems", arena.getStartedItems()));
		arena.setSpawns((ArrayList<Location>) conf.get("Spawns", arena.getSpawns()));
		arena.setAllowedToBreakBlocks((ArrayList<String>) conf.get("AllowedBlocks", arena.getAllowedToBreakBlocks()));
		arena.setOnlyAllowedBlocks(conf.getBoolean("OnlyAllowedBlocks", arena.isOnlyAllowedBlocks()));
		arena.setCreator(UUID.fromString(conf.getString("Creator", "")));
		arena.setBestPlayer(UUID.fromString(conf.getString("BestPlayer", "")));
		arena.setLobbyLocation((Location) conf.get("LobbyLocation", arena.getLobbyLocation()));
		arena.setSpectateLocation((Location) conf.get("WatchPointLocation", arena.getSpectateLocation()));
		arena.setPVPEnabled(conf.getBoolean("EnabledPVP", arena.isPVPEnabled()));
		arena.setWelcomeMessage(conf.getString("WelcomeMessage", arena.getWelcomeMessage()));
		arenasByName.put(name, arena);
		return true;
	}
	
	public void savePlayerData(Player p)
	{
		File temp = new File(getDataFolder() + File.separator);
		if (!temp.exists()) {temp.mkdir();}
		temp = new File(getDataFolder() + File.separator + "Players");
		if (!temp.exists()) {temp.mkdir();}
		
		temp = new File(getDataFolder() + File.separator + "Players" + File.separator + p.getUniqueId() + ".data");
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Location", p.getLocation());
		data.put("DisplayName", p.getDisplayName());
		data.put("InventoryContents", p.getInventory().getContents());
		data.put("ArmorContents", p.getInventory().getArmorContents());
		data.put("HP", p.getHealth());
		data.put("FOOD", p.getFoodLevel());
		data.put("isFlying", p.isFlying());
		data.put("GameMode", p.getGameMode());
		data.put("PotionEffects", p.getActivePotionEffects());
		data.put("LEVEL", p.getLevel());
		data.put("EXP", p.getExp());
		data.put("WalkSpeed", p.getWalkSpeed());
		data.put("FlySpeed", p.getFlySpeed());
		data.put("AllowFlight", p.getAllowFlight());
		
		try 
		{
			ObjectOutputStream ois = new BukkitObjectOutputStream(new FileOutputStream(temp));
			ois.writeObject(data); ois.flush(); ois.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void loadPlayerData(Player p)
	{
		File temp = new File(getDataFolder() + File.separator + "Players");
		
		temp = new File(getDataFolder() + File.separator + "Players" + File.separator + p.getUniqueId() + ".data");
		if (temp.exists())
		{
			try 
			{
				ObjectInputStream ois = new BukkitObjectInputStream(new FileInputStream(temp));
				HashMap<String, Object> data = (HashMap<String, Object>) ois.readObject();
				ois.close();
				
				p.setDisplayName((String) data.getOrDefault("DisplayName", p.getDisplayName()));
				p.getInventory().setContents((ItemStack[]) data.getOrDefault("InventoryContents", p.getInventory().getContents()));
				p.getInventory().setArmorContents((ItemStack[]) data.getOrDefault("ArmorContents", p.getInventory().getArmorContents()));
				p.setHealth((double) data.getOrDefault("HP", p.getHealth()));
				p.setFoodLevel((int) data.getOrDefault("FOOD", p.getFoodLevel()));
				p.setGameMode((GameMode) data.getOrDefault("GameMode", p.getGameMode()));
				p.addPotionEffects((Collection<PotionEffect>) data.getOrDefault("PotionEffects", p.getActivePotionEffects()));
				p.setLevel((int) data.getOrDefault("LEVEL", p.getLevel()));
				p.setExp((float) data.getOrDefault("EXP", p.getExp()));
				p.setWalkSpeed((float) data.getOrDefault("WalkSpeed", p.getWalkSpeed()));
				p.setFlySpeed((float) data.getOrDefault("FlySpeed", p.getFlySpeed()));
				p.teleport((Location) data.getOrDefault("Location", p.getBedSpawnLocation()));
				p.setAllowFlight((boolean) data.getOrDefault("AllowFlight", p.getAllowFlight()));
				if (p.getAllowFlight()) {p.setFlying((boolean) data.getOrDefault("isFlying", p.isFlying()));}
			} 
			catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
		}
	}
	
	public void clearPlayer(Player p)
	{
		p.setDisplayName(p.getName());
		p.getInventory().setArmorContents(null);
		p.getInventory().clear();
		p.setMaxHealth(20);
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setGameMode(GameMode.ADVENTURE);
		p.setLevel(60);
		p.setExp(0);
		p.setFlying(false);
		p.setWalkSpeed(0.2F);
		p.setFlySpeed(0.2F);
		for(PotionEffect pi : p.getActivePotionEffects()) {p.removePotionEffect(pi.getType());}
	}
	
	public boolean isArmor(ItemStack item) {return (CraftItemStack.asNMSCopy(item).getItem() instanceof ItemArmor);}
}