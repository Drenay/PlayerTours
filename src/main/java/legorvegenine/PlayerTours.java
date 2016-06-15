package legorvegenine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerTours extends JavaPlugin implements Listener
{
	ArrayList<Tour> tours = new ArrayList<Tour>();
	
	//Variables to store the file and its configuration
	FileConfiguration dataCFG;
	File data;
	
	@Override
	public void onEnable()
	{
		//Creates the configuration file with 3 variables (x, y, z)
		getConfig().addDefault("newPlayerSpawnX", 0);
		getConfig().addDefault("newPlayerSpawnY", 65);
		getConfig().addDefault("newPlayerSpawnZ", 0);
		getConfig().addDefault("serverName", "the server");
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		//If the data folder does not exist, we need to make it
		if (!getDataFolder().exists())
		{
			getDataFolder().mkdir();
		}
		
		//The file is in the plugin's data folder, named data.yml
		data = new File(getDataFolder(), "data.yml");
		
		//If the file does not exist, we need to create it
		if (!data.exists()) 
		{
			try 
			{
				//Create a new file to store data
				data.createNewFile();
            }
            catch (IOException e)
			{
            	//If the file is unable to be created, warn the console!
            	getLogger().severe(ChatColor.RED + "Could not create data.yml!");
            }
        }

		//Set the data file configuration to the file data
		dataCFG = YamlConfiguration.loadConfiguration(data);
		
		//Registers the event listener for player joins
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		//Save the configuration file upon unloading the plugin
		saveConfig();
		saveData();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender == getServer().getConsoleSender())
		{
			//Console Tour Count Command: Allows the console to access the player tours statistic
			if (cmd.getName().equalsIgnoreCase("tour"))
			{
				//If the console correctly issues the command, send the message
				if (args.length == 2 && args[0].equalsIgnoreCase("count"))
					getLogger().info(args[1] + " has given " + dataCFG.getInt(args[1]) + " tours.");
				//Otherwise, send the correct usage
				else
					getLogger().info("Usage: /tour count <playername>");
			}
			return true;
		}

		//Cast the sender as a player to be used later
		Player player = (Player)sender;
		
		//SetNewPlayerSpawn Command: Changes the coordinates of where new players spawn in at
		if (cmd.getName().equalsIgnoreCase("setnewplayerspawn") && sender instanceof Player)
		{	
			if (!player.hasPermission("cansetnewplayerspawn"))
			{
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
				
			if (args.length == 3)
			{
				try
				{
					//Try to convert the arguments to double values representative of coordinates
					Double x = Double.valueOf(args[0]);
					Double y = Double.valueOf(args[1]);
					Double z = Double.valueOf(args[2]);
					
					//Sets the 3 variables in the config file
					getConfig().set("newPlayerSpawnX", x);
					getConfig().set("newPlayerSpawnY", y);
					getConfig().set("newPlayerSpawnZ", z);
					saveConfig();
					reloadConfig();
					player.sendMessage(ChatColor.GREEN + "New Player Spawn set!");
				}
				catch(NumberFormatException e)
				{
					//Messages the player if they entered invalid arguments
					player.sendMessage(ChatColor.RED + "Invalid arguments. Use /setnewplayerspawn <x> <y> <z>");
				}
			}
			else
			{
				//Messages the player if they incorrectly entered the command arguments
				player.sendMessage(ChatColor.RED + "Incorrect arguments. Use /setnewplayerspawn <x> <y> <z>");
			}
			return true;
		}
		
		//SetServerName Command: Sets the name of the server for the new player welcome message
		if (cmd.getName().equalsIgnoreCase("setservername") && sender instanceof Player)
		{
			//If a player can set the spawn, they can also set the name
			if (!player.hasPermission("cansetnewplayerspawn"))
			{
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			
			//The user must specify arguments for the server name
			if (args.length == 0)
				player.sendMessage(ChatColor.RED + "Please specify a server name.");
			else
			{
				//Iterate through the args and concatenate them
				String s = "";
				for (String arg : args)
				{
					s += arg + " ";
				}
				s.trim();
				
				//Set the server's name in config
				getConfig().set("serverName", s);
				//Send the player a message
				player.sendMessage(ChatColor.GREEN + "Server name set to: " + s);
			}
			return true;
		}
		
		//Tour Command: Handles touring
		if (cmd.getName().equalsIgnoreCase("tour") && sender instanceof Player)
		{	
			if (args.length == 1)
			{
				//Tour List Command: Lists the current tours
				if (args[0].equalsIgnoreCase("list"))
				{
					//Send a header message with the number of tours
					player.sendMessage("Current Tours: (" + tours.size() + ")");
					for(Tour tour : tours)
					{
						//List out each tour
						player.sendMessage(tour.toString());
					}
					return true;
				}
				
				/* Additional Convenience Tour Commands
				if (args[0].equalsIgnoreCase("start"))
				{
					player.sendMessage(ChatColor.DARK_RED + "Not implemented yet.");
					return true;
				}
				
				if (args[0].equalsIgnoreCase("end"))
				{
					player.sendMessage(ChatColor.DARK_RED + "Not implemented yet.");
					return true;
				}
				*/
			}
			
			//Tour Start Command: Starts a tour
			if (args.length == 2 && args[0].equalsIgnoreCase("start"))
			{
				for(Tour tour : tours)
				{	
					//If the tour starts, announce it
					if (tour.startTour(player, args[1]))
					{
						getServer().broadcastMessage(ChatColor.GREEN + tour.tourGuide.getName() + " is now giving " + tour.newPlayer.getName() + " a tour.");
					}
					else
					{
						//If an invalid player name is provided
						if (!args[1].equalsIgnoreCase(tour.newPlayer.getName()))
							player.sendMessage(ChatColor.RED + "You cannot give a tour to " + args[1] + ".");
						//If there is already a tour guide assigned
						else if (tour.tourGuide != null)
							player.sendMessage(ChatColor.RED + tour.tourGuide.getName() + " is already giving a tour.");
						//If anything else goes wrong
						else
							player.sendMessage(ChatColor.RED + "Could not start the tour.");
					}
				}
				return true;
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("end"))
			{
				for(Tour tour : tours)
				{
					//If the tour ends, announce it
					if (tour.endTour(player, args[1]))
					{
						getServer().broadcastMessage(ChatColor.GREEN + tour.tourGuide.getName() + " has finished giving " + tour.newPlayer.getName() + " a tour.");
						
						fireworks(tour.newPlayer, 2);
						fireworks(tour.tourGuide, 2);
						
						//Increase the tour guide's tour statistic
						incrementTourStat(tour.tourGuide);
						tours.remove(tour);
						break;
					}
					else
					{
						//If an invalid player name is provided
						if (!args[1].equalsIgnoreCase(tour.newPlayer.getName()))
							player.sendMessage(ChatColor.RED + "You cannot end a tour with " + tour.newPlayer.getName() + ".");
						//If the tour guide is not the one ending the tour
						else if (tour.tourGuide != player)
							player.sendMessage(ChatColor.RED + "You cannot end a tour you are not giving");
						//If anything else goes wrong
						else
							player.sendMessage(ChatColor.RED + "Could not end the tour.");
					}
				}
				return true;
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("count"))
			{
				if (!player.hasPermission("cancheckplayertourstats"))
				{
					player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
					return true;
				}
				//Tell the player how many tours a specific player has given.
				player.sendMessage(args[1].toLowerCase() + " has given " + dataCFG.getInt(args[1]) + " tours.");
				return true;
			}
			
			//Message the player the correct usage if nothing else succeeds
			player.sendMessage("Correct Usage for /tour:");
			player.sendMessage(ChatColor.GREEN + "/tour list" + ChatColor.WHITE + " to see a list of the ongoing tours.");
			player.sendMessage(ChatColor.GREEN + "/tour start <playername>" + ChatColor.WHITE + " to start a tour with a new player.");
			player.sendMessage(ChatColor.GREEN + "/tour end <playername>" + ChatColor.WHITE + " to end a tour you are giving.");
			player.sendMessage(ChatColor.GREEN + "/tour count <playername>" + ChatColor.WHITE + " to see the number of tours a player has given.");
			
			//If any of the above commands pass, return true
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		
		//Check if the p has played before
		if (!p.hasPlayedBefore())
		{
			if (!tours.contains(new Tour(p)))
			{
				//Add the p as a new p
				tours.add(new Tour(p));
				//Welcome the p
				e.setJoinMessage(ChatColor.BOLD + " " + ChatColor.YELLOW + "Welcome " + p.getName() + " to " + getConfig().getString("serverName") + "!");
				
				//Generate 4 fireworks when a new p joins!
				fireworks(p, 4);
				
				//Loads the variables from the config
				int x = getConfig().getInt("newPlayerSpawnX");
				int y = getConfig().getInt("newPlayerSpawnY");
				int z = getConfig().getInt("newPlayerSpawnZ");
				
				//Teleports the new p to spawn
				Location newPlayerSpawn = new Location(p.getWorld(), x, y, z);
				p.teleport(newPlayerSpawn);
			}
		}	
	}
	
	public void fireworks(Player p, int count)
	{
		//Counter for the number of fireworks to be spawned
		for(int i = 0; i < count; i++)
		{
			//Create a random number generator
			Random r = new Random();
			//Generate 2 random colors
			Color c1 = Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			Color c2 = Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			//Array of firework types to randomize
			Type[] types = {Type.BALL, Type.BALL_LARGE, Type.BURST, Type.CREEPER, Type.STAR};
			
			//Create the firework at the player's location
			Firework f = (Firework) p.getWorld().spawn(p.getLocation(), Firework.class);
			//Edit the firework's meta
			FireworkMeta fm = f.getFireworkMeta();
			fm.addEffect(FireworkEffect.builder()
					.flicker(r.nextBoolean())				//Randomly decide if the firework should flicker
					.trail(r.nextBoolean())					//Randomly decide if the firework should have a trail
					.with(types[r.nextInt(types.length)])	//Randomly decide the firework's type
					.withColor(c1)							//Assign the random primary color
					.withFade(c2)							//Assign the random secondary color
					.build());
			fm.setPower(r.nextInt(3) + 1);					//Randomly decide the firework's power level
			//Apply the firework meta to the firework
			f.setFireworkMeta(fm);
		}
	}
	
	public void incrementTourStat(Player p)
	{
		//Convert the player's name into a string
		String n = p.getName().toLowerCase();

		//If no entry exists for a player, add one
		if (dataCFG.isSet(n))
			dataCFG.set(n, dataCFG.getInt(n) + 1);
		//Otherwise add one
		else
			dataCFG.set(n, 1);
		
		//Save the data, of course
		saveData();
	}
		
	public void saveData() 
	{
		//Try to save the data
		try 
		{
			dataCFG.save(data);
		}
		catch (IOException e) 
		{
			//If the file is unable to be saved, warn the console!
			getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
		}
		
		//Reload the configuration as well
		dataCFG = YamlConfiguration.loadConfiguration(data);
	}
}