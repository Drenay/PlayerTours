package legorvegenine;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Tour 
{
	Player tourGuide = null;
	Player newPlayer = null;
	Location previousLocation = null;
	
	public Tour(Player p)
	{
		newPlayer = p;
	}
	
	public boolean startTour(Player p, String playername)
	{
		if(!playername.equalsIgnoreCase(newPlayer.getName())) return false;
		if(tourGuide != null) return false;
		
		tourGuide = p;
		previousLocation = p.getLocation();
		
		tourGuide.teleport(newPlayer);
		playerGlow(tourGuide, true);
		playerGlow(newPlayer, true);
		return true;
	}
	
	public boolean endTour(Player p, String playername)
	{
		if(!playername.equalsIgnoreCase(newPlayer.getName())) return false;
		if(tourGuide != p) return false;
		
		tourGuide.teleport(previousLocation);
		playerGlow(tourGuide, false);
		playerGlow(newPlayer, false);
		return true;
	}
	
	public void playerGlow(Player p, boolean on)
	{
		if (on)
			p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 36000, 0));
		else
			p.removePotionEffect(PotionEffectType.GLOWING);
	}
	
	public String toString()
	{
		if (tourGuide == null)
			return "Nobody is giving " + newPlayer.getName() + " a tour.";
		return tourGuide.getName() + " is giving " + newPlayer.getName() + " a tour.";
	}
}

//GITHUB TEST LMAO
