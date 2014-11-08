package ca.carbogen.korra.LavaSlap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;

public class LavaSlap 
{
	public final static int range = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.LavaSlap.range");
	public final static int cooldown = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.LavaSlap.cooldown");
	public final static int delay = 100;
	public final static int executeTime = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.LavaSlap.cleanupDelay");
	public final static double PARTICLE_DENSITY = ProjectKorra.plugin.getConfig().getInt("ExtraAbilities.Carbogen.LavaSlap.particleDensity");
	// Unused
	//private final double LAVA_CREATE_SPEED = 0.1;
	private Player player;
	private Location shiftSource;
	private long time;
	private boolean isShift;
	private boolean isFinished = false;
	private int curIndex = -1;
	private List<Block> affectedBlocks = new ArrayList<Block>();
	private List<LivingEntity> affectedEntities = new ArrayList<LivingEntity>();
	private List<TempBlock> convertedBlocks = new ArrayList<TempBlock>();
	public static Map<Player, LavaSlap> instances = new ConcurrentHashMap<Player, LavaSlap>();
	
	public LavaSlap(Player player, boolean shift)
	{
		this.isShift = shift;
		
		if(shift)
		{
			this.player = player;
			
			instances.put(player, this);
						
			shiftSource = Methods.getTargetedLocation(player, range/2);
			
			//ProjectKorra.log.info("Shift LavaSlap instantiated.");
		}
		
		else if(!shift)
		{
			this.player = player;
			
			time = System.currentTimeMillis();
			
			instances.put(player, this);
			
			//Entity target = Methods.getTargetedEntity(player, range, null);
			
			loadAffectedBlocks(player.getLocation(), Methods.getTargetedLocation(player, range));
	
			Methods.getBendingPlayer(player.getName()).addCooldown("LavaSlap", cooldown);
		}
	}
	
	public void specifyDestination(Location dest)
	{
		if(isShift)
		{
			time = System.currentTimeMillis();

			loadAffectedBlocks(shiftSource, dest);
			
			//ProjectKorra.log.info("Shift LavaSlap ready to roll.");

			Methods.getBendingPlayer(player.getName()).addCooldown("LavaSlap", cooldown);
		}
	}
	
	public void loadAffectedBlocks(Location source, Location dest, Vector direction)
	{
		source.add(0, -1, 0);
		dest.add(0, -1, 0);
		dest.setY(dest.getBlockY());
		
		BlockIterator bi = new BlockIterator(source.getWorld(), source.toVector(), direction, 0, range + 3);
		
		if(!bi.hasNext())
			//ProjectKorra.log.info("bi is empty!");
		
		while(bi.hasNext())
		{
			Block b = bi.next();
			
			if(/*b.getY() == source.getBlockY()*/ true)
			{
				if(Methods.isEarthbendable(player, b))
				{
					affectedBlocks.add(b);
				}
			}
		}

		addWidth();
		reorderAffectedBlocks(source, dest);
	}
	
	public void loadAffectedBlocks(Location source, Location dest)
	{
		source.add(0, -1, 0);
		dest.add(0, -1, 0);
		//dest.setY(dest.getBlockY());
		
		BlockIterator bi = new BlockIterator(source.getWorld(), source.toVector(), Methods.getDirection(source, dest), 0, range);
		
		if(!bi.hasNext())
			ProjectKorra.log.info("bi is empty!");
		
		while(bi.hasNext())
		{
			Block b = bi.next();
			
			if(/*b.getY() == source.getBlockY()*/ true)
			{
				if(Methods.isEarthbendable(player, b))
				{
					affectedBlocks.add(b);
				}
				
				for(Entity e : Methods.getEntitiesAroundPoint(b.getLocation(), 2))
				{
					if(e instanceof LivingEntity)
					{
						affectedEntities.add((LivingEntity) e);
					}
				}

				addDepth(b);
			}
		}

		addWidth();
		reorderAffectedBlocks(source, dest);
	}
	
	public void addWidth()
	{
		int size = affectedBlocks.size();
		
		for(int i = 0; i < size; i++)
		{
			Block b = affectedBlocks.get(i);
			List<Block> near = Methods.getBlocksAroundPoint(b.getLocation(), 1);
			for(Block nbb : near)
			{
				if(!affectedBlocks.contains(nbb))
				{
					if(/*nbb.getY() == b.getY() &&*/ Methods.isEarthbendable(player, nbb))
					{
						affectedBlocks.add(nbb);
						//ProjectKorra.log.info("Added " + nbb.getLocation());
					}

					addDepth(nbb);
				}
			}
		}
	}
	
	public void addDepth(Block b)
	{
		Block down = b.getRelative(BlockFace.DOWN);
		
		if(Methods.isEarthbendable(player, down))
		{
			affectedBlocks.add(down);
		}
	}
	
	public void reorderAffectedBlocks(Location source, Location dest)
	{
		List<Block> abs = new ArrayList<Block>();
		
		for(int i = 2; i < range; i++)
		{
			for(int j = 0; j < affectedBlocks.size(); j++)
			{
				Block b = affectedBlocks.get(j);
				if(b.getLocation().distance(source) <= i)
				{
					if(b.getLocation().distance(source) >= 2)
					{
						abs.add(b);
						affectedBlocks.remove(b);
					}
				}
			}
		}
		
		this.affectedBlocks = abs;
	}
	
	public void playEffects()
	{
		if(!player.isOnline() || player.isDead())
		{
			remove();
			return;
		}
			
		for(Block b : this.affectedBlocks)
		{
			if(b != null && Math.random() < PARTICLE_DENSITY)
			{
				if(b.getRelative(BlockFace.UP).getType().isTransparent())
				{
					ParticleEffect.LAVA.display(b.getLocation(), 0, 0, 0, 0, 1);
				}
			}
		}
	}
	
	public void progress()
	{
		if(!player.isOnline() || player.isDead())
		{
			remove();
			return;
		}
		
		long curTime = System.currentTimeMillis();
		
		if(isShift && isFinished && curTime > time + delay + executeTime)
		{
			remove();
			return;
		}
		
		else if(isFinished && curTime > time + delay + executeTime)
		{
			remove();
			return;
		}
		
		if(!isFinished && curTime > time + delay && curTime < time + delay + executeTime)
		{
			isFinished = true;
			// Unused.
			//double rand = Math.random();
			
			Block block;
			
			try { block = getNextBlock(); }
			catch(Exception e) { return; }

			if(block != null)
			{
				Location loc = block.getLocation();
				
				if(!Methods.isRegionProtectedFromBuild(player, "LavaSlap", loc))
				{
					if(!isLava(block))
					{
						turnToLava(block);
					}
				}
			}
			
			for(Block b : affectedBlocks)
			{
				if(!isLava(b))
				{
					isFinished = false;
					ParticleEffect.LAVA.display(b.getLocation(), 0, 0, 0, 0, 1);
				}
			}
			
			// Old Code - Results not optimal.
			/*for(Block b : this.affectedBlocks)
			{
				Location loc = b.getLocation();
				
				if(!Methods.isRegionProtectedFromBuild(player, "LavaSlap", loc))
				{
					if(b.getType() != Material.STATIONARY_LAVA && b.getType() != Material.LAVA)
					{
						isFinished = false;
						if(rand < LAVA_CREATE_SPEED)
						{
							turnToLava(b);
							rand = Math.random();
						}
						
						else
						{
							ParticleEffect.LAVA.display(loc, 0, 0, 0, 0, 1);
						}
					}
				}
			}*/
		}
	}
	
	public static void playAllEffects()
	{
		long curTime = System.currentTimeMillis();
		for(Player p : instances.keySet())
		{
			if(curTime < instances.get(p).time + delay)
			{
				instances.get(p).playEffects();
			}
		}
	}
	
	public boolean isLava(Block block)
	{
		if(block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
			return true;
		return false;
	}
	
	public void turnToLava(Block block)
	{
		TempBlock tblock = new TempBlock(block, Material.STATIONARY_LAVA, (byte) 0);
		convertedBlocks.add(tblock);
	}
	
	public void revertLava()
	{
		for(TempBlock tb : this.convertedBlocks)
		{
			tb.revertBlock();
		}
		
		convertedBlocks.clear();
	}
	
	public Block getNextBlock()
	{
		curIndex++;
		return affectedBlocks.get(curIndex);
	}
	
	public boolean isShift()
	{
		return isShift;
	}
	
	public List<LivingEntity> getAffectedEntities()
	{
		return this.affectedEntities;
	}
	
	public void remove()
	{
		revertLava();
		affectedBlocks.clear();
		affectedEntities.clear();
		instances.remove(player);
	}
	
	public static void progressAll()
	{
		for(Player p : instances.keySet())
		{
			instances.get(p).progress();
		}
	}
}
