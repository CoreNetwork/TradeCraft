package us.corenetwork.tradecraft;


public enum Setting {	

    CURRENCY("Currency", "emerald"),
    ALL_UNLOCKED_REFRESH_CHANCE("AllUnlockedRefreshChance", 0.2),
	DEFAULT_PROFESSION_COLOR("DefaultProfessionColor", ""),

    AI_LIMITER_ENABLE("AILimiter.Enable", false),
    AI_LIMITER_DISTANCE_TO_PLAYER("AILimiter.MaximumDistanceToPlayer", 32),
	KEEP_VANILLA_IF_NO_TRADES_SPECIFIED("KeepVanillaTradesIfNoneSpecified", false),
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
