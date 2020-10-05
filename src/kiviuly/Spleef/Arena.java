package kiviuly.Spleef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Arena
{
	private Main main = Main.main;
	
	private String ID = "";
	private String name = "";
	private String description = "";
	
	private String welcomeMessage = "";
	
	private int minPlayers = 1;
	private int maxPlayers = 12;
	
	private UUID Creator = null;
	private UUID BestPlayer = null;
	
	private boolean isEnabled = false;
	private boolean pvpEnabled = false;
	private boolean onlyAllowedBlocks = true;
	
	private ArrayList<UUID> PlayersInGame = new ArrayList<>();
	private ArrayList<UUID> PlayersInLobby = new ArrayList<>();
	private ArrayList<UUID> PlayersInSpectators = new ArrayList<>();
	
	private ArrayList<Location> Spawns = new ArrayList<>();
	private ArrayList<String> AllowedToBreakBlocks = new ArrayList<>();
	private ArrayList<ItemStack> StartedItems = new ArrayList<>();

	private HashMap<UUID, Double> lobbyDamage = new HashMap<>();
	private HashMap<Location, Material> modifedBlocks = new HashMap<>();
	
	private Location lobbyLocation = null;
	private Location spectateLocation = null;
	
	private ArenaStatus status = ArenaStatus.Waiting;
	
	private BossBar bossbar = Bukkit.createBossBar("§e§lОжидание...", BarColor.WHITE, BarStyle.SOLID);
	
	public Arena(String ID, UUID creator)
	{
		this.ID = ID;
		this.Creator = creator;
		this.BestPlayer = creator;
		this.name = ID;
		
		AllowedToBreakBlocks.add(Material.TNT.name());
		AllowedToBreakBlocks.add(Material.SNOW_BLOCK.name());
		
		StartedItems.add(new ItemBuilder(Material.DIAMOND_SPADE).displayname("§e§lОт винта!").build());
		StartedItems.add(new ItemBuilder(Material.LEATHER_CHESTPLATE).displayname("§1Gucci §2Flip §4Flap").build());
	}
	
	public void Start(int time)
	{
		if (!isEnabled) return;
		if (isStarted()) return;
		Arena arena = this;
		
		new BukkitRunnable()
		{
			int sec = time;
			
			@Override
			public void run()
			{
				if (PlayersInLobby.size() < minPlayers) {setStatus(ArenaStatus.Waiting); cancel();}
				
				if (sec == 0)
				{
					ArrayList<Location> list = new ArrayList<>();
					setStatus(ArenaStatus.Playing);
					
					bossbar.setColor(BarColor.BLUE);
					bossbar.setTitle("§9Идёт игра...");
					bossbar.setProgress(1.0);
					
					for(UUID id : PlayersInLobby)
					{
						Player p = Bukkit.getPlayer(id);
						if (p == null) {continue;}
						PlayersInGame.add(id);
						p.setGameMode(GameMode.SURVIVAL);
						main.setPlayerArena(p, arena);
						
						for(ItemStack is : StartedItems)
						{
							if (is == null) {continue;}
							if (is.getType().name().endsWith("_HELMET")) {p.getInventory().setHelmet(is);} else
							if (is.getType().name().endsWith("_CHESTPLATE")) {p.getInventory().setChestplate(is);} else
							if (is.getType().name().endsWith("_LEGGINGS")) {p.getInventory().setLeggings(is);} else
							if (is.getType().name().endsWith("_BOOTS")) {p.getInventory().setBoots(is);} else
							{p.getInventory().addItem(is);}
						}
						
						p.sendTitle("§3§lВскопай их всех", "§7***");
						Location l = Spawns.get(main.randomInt(0, Spawns.size()-1));
						for(int i = 0; i < 500; i++)
						{if (list.contains(l)) {l = Spawns.get(main.randomInt(0, Spawns.size()-1));} else {break;}}
						p.teleport(l);
					}
					
					PlayersInLobby.clear();
					cancel();
					return;
				}
				
				if (sec <= 10 || sec % 10 == 0)
				{
					for(UUID id : PlayersInLobby)
					{
						Player p = Bukkit.getPlayer(id);
						if (p == null) {continue;}
						p.sendTitle("§eНачало через " + sec + "...", "");
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 10/sec);
					}
				}
				
				double progress = (1.0*sec)/(time*1.0);
				bossbar.setColor(BarColor.YELLOW);
				bossbar.setTitle("§eНачало через " + sec + "...");
				bossbar.setProgress(progress); sec--;
				setStatus(ArenaStatus.Starting);
			}
			
		}.runTaskTimer(main, 20L, 20L);
		
		setStatus(ArenaStatus.Starting);
	}
	
	public void Stop(int time)
	{
		if (!isEnabled) return;
		if (!isStarted()) return;
		
		setStatus(ArenaStatus.Ending);
		ArrayList<UUID> list = new ArrayList<>();
		list.addAll(PlayersInGame); 
		list.addAll(PlayersInSpectators);
		list.addAll(PlayersInLobby);
		
		for(UUID id : list)
		{
			Player p = Bukkit.getPlayer(id);
			if (p == null) {continue;}
			if (PlayersInGame.size() > 1 || PlayersInGame.size() == 0) {p.sendTitle("§e§lНичья", "§7Выжило больше 1 игрока");}
			if (PlayersInGame.size() == 1) 
			{
				Player pl = Bukkit.getPlayer(PlayersInGame.get(0));
				if (pl == null)	{p.sendTitle("§e§lНичья", "§7Выжило больше 1 игрока");}
				
				if (pl.getName().equals(p.getName())) {p.sendTitle("§6§lПобеда", "§eТы оказался самым живучим!");}
				else {p.sendTitle("§c§lПоражение", "§eКто-то оказался лучше...");}
			}
		}
		
		new BukkitRunnable()
		{
			int sec = time;
			
			@Override
			public void run()
			{
				if (sec == 0)
				{
					for(UUID id : list)
					{
						Player p = Bukkit.getPlayer(id);
						if (p == null) {continue;}
						bossbar.removePlayer(p);
						main.clearPlayer(p);
						main.loadPlayerData(p);
						main.setPlayerArena(p, null);
					}
					
					for(Location l : modifedBlocks.keySet())
					{
						Material m = modifedBlocks.get(l);
						l.getBlock().setType(m);
					}
					
					PlayersInGame.clear();
					PlayersInLobby.clear();
					PlayersInSpectators.clear();
					lobbyDamage.clear();
					modifedBlocks.clear();
					bossbar.removeAll();
					bossbar.setTitle("§e§lОжидание...");
					bossbar.setColor(BarColor.WHITE);
					setStatus(ArenaStatus.Waiting);
					cancel();
					return;
				}
				
				if (sec <= 10)
				{
					for(UUID id : list)
					{
						Player p = Bukkit.getPlayer(id);
						if (p == null) {continue;}
						p.sendTitle("§2Возврат в лобби через " + sec + "...", "");
						p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 10/sec);
					}
				}
				
				double progress = (1.0*sec)/(time*1.0);
				bossbar.setColor(BarColor.GREEN);
				bossbar.setTitle("§2Возврат в лобби через " + sec + "...");
				bossbar.setProgress(progress); sec--;
				setStatus(ArenaStatus.Ending);
			}
			
		}.runTaskTimer(main, 20L, 20L);
	}

	public void SendMessage(String msg)
	{
		if (getStatus().equals(ArenaStatus.Waiting) || getStatus().equals(ArenaStatus.Starting))
		{
			for(UUID id : PlayersInLobby)
			{
				Player p = Bukkit.getPlayer(id);
				if (p == null) {PlayersInLobby.remove(id); continue;}
				p.sendMessage(msg);
			}
		}
		
		if (getStatus().equals(ArenaStatus.Playing) || getStatus().equals(ArenaStatus.Ending))
		{
			for(UUID id : getAllPlayers())
			{
				Player p = Bukkit.getPlayer(id);
				if (p == null) {continue;}
				p.sendMessage(msg);
			}
		}
	}

	public ArrayList<UUID> getAllPlayers()
	{
		ArrayList<UUID> ids = (ArrayList<UUID>) PlayersInGame.clone();
		ids.addAll(PlayersInSpectators);
		return ids;
	}

	public boolean addPlayer(Player p)
	{
		if (!main.getPlayerStatus(p).equals(Status.NotInGame)) return false;
		if (!isEnabled) return false;
		if (PlayersInLobby.size() == maxPlayers) return false;
		if (isPlaying(p) || isWaiting(p) || isSpectator(p)) return false;
		PlayersInLobby.add(p.getUniqueId());
		main.savePlayerData(p);
		main.clearPlayer(p);
		main.setPlayerArena(p, this);
		bossbar.addPlayer(p);
		p.teleport(getLobbyLocation());
		p.sendMessage(getWelcomeMessage());
		
		if (PlayersInLobby.size() == minPlayers) {Start(15);}
		return true;
	}
	
	public void addDeadPlayer(Player p)
	{
		if (!isPlaying(p)) return;
		PlayersInGame.remove(p.getUniqueId());
		PlayersInSpectators.add(p.getUniqueId());
		main.clearPlayer(p);
		
		ItemStack is = new ItemBuilder(Material.SLIME_BALL)
			.displayname("§6Свободное наблюдение")
			.lore("§fНажмите §eПКМ")
			.lore("§fЧтобы включить")
			.lore("§fРежим свободного полёта")
			.enchant(Enchantment.LUCK, 2).build();
		
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setGameMode(GameMode.ADVENTURE);
		p.teleport(spectateLocation);
		p.getInventory().setItem(4, is);
		if (PlayersInGame.size() < 2) {Stop(15);}
	}

	public boolean removePlayer(Player p)
	{
		if (main.getPlayerStatus(p).equals(Status.NotInGame)) return false;
		if (!isPlaying(p) && !isWaiting(p) && !isSpectator(p)) return false;
		bossbar.removePlayer(p);
		
		if (getPlayersInLobby().contains(p.getUniqueId()))
		{
			PlayersInLobby.remove(p.getUniqueId());
			main.clearPlayer(p);
			main.loadPlayerData(p);
			main.setPlayerArena(p, null);
		}
		
		if (getPlayersInGame().contains(p.getUniqueId()))
		{
			PlayersInGame.remove(p.getUniqueId());
			main.loadPlayerData(p);
			main.setPlayerArena(p, null);
			if (PlayersInGame.size() == 1) {Stop(15);}
		}
		
		if (getPlayersInSpectators().contains(p.getUniqueId()))
		{
			PlayersInSpectators.remove(p.getUniqueId());
			main.loadPlayerData(p);
			main.setPlayerArena(p, null);
		}
		return true;
	}

	public String getID()
	{
		return ID;
	}
	public void setID(String iD)
	{
		ID = iD;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	public int getMinPlayers()
	{
		return minPlayers;
	}
	public void setMinPlayers(int minPlayers)
	{
		this.minPlayers = minPlayers;
	}
	public int getMaxPlayers()
	{
		return maxPlayers;
	}
	public void setMaxPlayers(int maxPlayers)
	{
		this.maxPlayers = maxPlayers;
	}
	public boolean isEnabled()
	{
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled = isEnabled;
	}
	public ArrayList<UUID> getPlayersInGame()
	{
		return PlayersInGame;
	}
	public void setPlayersInGame(ArrayList<UUID> playersInGame)
	{
		PlayersInGame = playersInGame;
	}
	public ArrayList<UUID> getPlayersInLobby()
	{
		return PlayersInLobby;
	}
	public void setPlayersInLobby(ArrayList<UUID> playersInLobby)
	{
		PlayersInLobby = playersInLobby;
	}
	public ArrayList<UUID> getPlayersInSpectators()
	{
		return PlayersInSpectators;
	}
	public void setPlayersInSpectators(ArrayList<UUID> playersInSpectators)
	{
		PlayersInSpectators = playersInSpectators;
	}
	public ArrayList<String> getAllowedToBreakBlocks()
	{
		return AllowedToBreakBlocks;
	}
	public void setAllowedToBreakBlocks(ArrayList<String> allowedToBreakBlocks)
	{
		AllowedToBreakBlocks = allowedToBreakBlocks;
	}
	public ArrayList<ItemStack> getStartedItems()
	{
		return StartedItems;
	}
	public void setStartedItems(ArrayList<ItemStack> startedItems)
	{
		StartedItems = startedItems;
	}

	public UUID getCreator()
	{
		return Creator;
	}

	public void setCreator(UUID creator)
	{
		Creator = creator;
	}

	public UUID getBestPlayer()
	{
		return BestPlayer;
	}

	public void setBestPlayer(UUID bestPlayer)
	{
		BestPlayer = bestPlayer;
	}

	public boolean isPlaying(Player p)
	{
		return PlayersInGame.contains(p.getUniqueId());
	}

	public boolean isWaiting(Player p)
	{
		return PlayersInLobby.contains(p.getUniqueId());
	}

	public boolean isSpectator(Player p)
	{
		return PlayersInSpectators.contains(p.getUniqueId());
	}

	public Location getLobbyLocation()
	{
		return lobbyLocation;
	}

	public void setLobbyLocation(Location lobbyLocation)
	{
		this.lobbyLocation = lobbyLocation;
	}

	public String getWelcomeMessage()
	{
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage)
	{
		this.welcomeMessage = welcomeMessage;
	}

	public ArenaStatus getStatus()
	{
		return status;
	}

	public void setStatus(ArenaStatus status)
	{
		this.status = status;
	}

	public boolean isStarted()
	{
		return getStatus().equals(ArenaStatus.Playing);
	}

	public HashMap<UUID, Double> getLobbyDamage()
	{
		return lobbyDamage;
	}

	public void setLobbyDamage(HashMap<UUID, Double> lobbyDamage)
	{
		this.lobbyDamage = lobbyDamage;
	}
	
	public void addLobbyDamage(UUID id, double dmg)
	{
		dmg += getLobbyDamage(id);
		lobbyDamage.put(id, dmg);
	}
	
	public double getLobbyDamage(UUID id)
	{
		return lobbyDamage.getOrDefault(id, 0.0);
	}

	public boolean isPVPEnabled()
	{
		return pvpEnabled;
	}

	public void setPVPEnabled(boolean pvpEnabled)
	{
		this.pvpEnabled = pvpEnabled;
	}

	public boolean isOnlyAllowedBlocks()
	{
		return onlyAllowedBlocks;
	}

	public void setOnlyAllowedBlocks(boolean onlyAllowedBlocks)
	{
		this.onlyAllowedBlocks = onlyAllowedBlocks;
	}

	public HashMap<Location, Material> getModifedBlocks()
	{
		return modifedBlocks;
	}

	public void setModifedBlocks(HashMap<Location, Material> modifedBlocks)
	{
		this.modifedBlocks = modifedBlocks;
	}
	
	public void addModifedBlock(Location l, Material m)
	{
		modifedBlocks.put(l, m);
	}

	public Location getSpectateLocation()
	{
		return spectateLocation;
	}

	public void setSpectateLocation(Location spectateLocation)
	{
		this.spectateLocation = spectateLocation;
	}

	public BossBar getBossbar()
	{
		return bossbar;
	}

	public void setBossbar(BossBar bossbar)
	{
		this.bossbar = bossbar;
	}
	
	public void addSpawn(Location l)
	{
		Spawns.add(l);
	}
	
	public void removeSpawn(Location l)
	{
		Spawns.remove(l);
	}

	public ArrayList<Location> getSpawns()
	{
		return Spawns;
	}

	public void setSpawns(ArrayList<Location> list)
	{
		Spawns = list;
	}
}
