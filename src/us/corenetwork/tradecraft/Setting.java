package us.corenetwork.tradecraft;


public enum Setting {	
	
	DEBUG("Debug", false),
	
	MESSAGE_NO_PERMISSION("Messages.NoPermission", "No permission!"),
	MESSAGE_CONFIGURATION_RELOADED("Messages.ConfigurationReloaded", "Configuration reloaded successfully!");

	private String name;
	private Object def;
	
	private Setting(String Name, Object Def)
	{
		name = Name;
		def = Def;
	}
	
	public String getString()
	{
		return name;
	}
	
	public Object getDefault()
	{
		return def;
	}
}
