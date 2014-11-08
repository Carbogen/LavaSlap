package ca.carbogen.korra.LavaSlap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModule;

public class LavaSlapInformation extends AbilityModule
{
	private Plugin pk;
	private FileConfiguration config;
	
	public LavaSlapInformation() 
	{
		super("LavaSlap");
	}
	
	public String getDescription()
	{
		return "LavaSlap developed by Carbogen. \n"
				+ "To use this ability, you must bind it to your hotbar with "
				+ "/bending bind LavaSlap. Afterwards, you may click the ground "
				+ "and a crevice filled with lava will erupt from the ground, progressing "
				+ "towards the block you aimed at. Additionally, you may alter the source of "
				+ "the ability by sneaking while looking at a block you'd like to designate as the "
				+ "source. Once you've done that, click again in the direction you want and watch the lava "
				+ "form.";
	}
	
	public String getAuthor()
	{
		return "Carbogen";
	}
	
	public String getVersion()
	{
		return "v1.0.3.2";
	}
	
	public String getElement()
	{
		return Element.Earth.toString();
	}
	
	public boolean isShiftAbility()
	{
		return true;
	}

	public boolean isHarmlessAbility()
	{
		return false;
	}
	
	public void onThisLoad()
	{
		loadConfig();
		config.addDefault("ExtraAbilities.Carbogen.LavaSlap.range", 16);
		config.addDefault("ExtraAbilities.Carbogen.LavaSlap.cooldown", 8000);
		config.addDefault("ExtraAbilities.Carbogen.LavaSlap.cleanupDelay", 6000);
		config.addDefault("ExtraAbilities.Carbogen.LavaSlap.particleDensity", 0.33);
		pk.saveConfig();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new LavaSlapListener(), ProjectKorra.plugin);
		ProjectKorra.plugin.getServer().getPluginManager().addPermission(new LavaSlapPermissions().lavaSlapDefault);
		ProjectKorra.plugin.getServer().getPluginManager().getPermission("bending.ability.LavaSlap").setDefault(PermissionDefault.FALSE);
		LavaSlapListener.manageLavaSlap();
		ProjectKorra.log.info(getName()+" by "+getAuthor()+" has been loaded.");
	}
	
	public void loadConfig()
	{
		pk = ProjectKorra.plugin;
		config = pk.getConfig();
	}
	
	public void stop()
	{
		ProjectKorra.plugin.getServer().getPluginManager().removePermission(new LavaSlapPermissions().lavaSlapDefault);
	}
}
