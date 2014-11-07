package ca.carbogen.korra.LavaSlap;

import org.bukkit.permissions.Permission;

public class LavaSlapPermissions 
{
	public Permission lavaSlapDefault;
	
	public LavaSlapPermissions()
	{
		super();
		lavaSlapDefault = new Permission("bending.ability.lavaslap");
	}
}
