package ca.carbogen.korra.LavaSlap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class LavaSlapListener implements Listener
{
	@EventHandler
	public void onPlayerShift(PlayerToggleSneakEvent e)
	{
		if(e.isSneaking())
		{
			if(!isEligible(e.getPlayer()))
				return;
			
			new LavaSlap(e.getPlayer(), true);
		}
	}
	
	@EventHandler
	public void onLeftClick(PlayerAnimationEvent e)
	{
		if(!isEligible(e.getPlayer()))
			return;
		
		if(!LavaSlap.instances.containsKey(e.getPlayer()))
			new LavaSlap(e.getPlayer(), false);
		
		else
			if(LavaSlap.instances.get(e.getPlayer()).isShift())
				LavaSlap.instances.get(e.getPlayer()).specifyDestination(Methods.getTargetedLocation(e.getPlayer(), LavaSlap.range));
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e)
	{
		if(e.getEntity() instanceof LivingEntity)
		{
			LivingEntity entity = (LivingEntity) e.getEntity();
			
			for(Player p : LavaSlap.instances.keySet())
			{
				if(LavaSlap.instances.get(p).getAffectedEntities().contains(entity))
				{
					p.giveExp(e.getDroppedExp());
					e.setDroppedExp(0);
				}
			}
		}
	}
	
	public boolean isEligible(Player player)
	{
		final BendingPlayer bplayer = Methods.getBendingPlayer(player.getName());
		
		if(!Methods.canBend(player.getName(), "LavaSlap"))
			return false;
		
		if(!Methods.getBoundAbility(player).equalsIgnoreCase("LavaSlap"))
			return false;
		
		if(Methods.isRegionProtectedFromBuild(player, "LavaSlap", player.getLocation()))
			return false;
		
		if(!Methods.canLavabend(player))
			return false;
		
		if(bplayer.isOnCooldown("LavaSlap"))
			return false;
		
		return true;
	}
	
	public static void manageLavaSlap()
	{
		BukkitRunnable br = new BukkitRunnable() 
		{
			public void run()
			{
				// 6x Speed
				for(int i = 0; i < 6; i++)
				{
					LavaSlap.progressAll();
				}
				
				LavaSlap.playAllEffects();
			}
		};
		
		ProjectKorra.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(ProjectKorra.plugin, br, 0, 1);
	}
}
