package sound;

public enum SoundTitle
{
	DEFAULT_TUNE("lalala", true),
	
	// Menu Tunes
	MAIN_MENU_TUNE("dancemix", true),
	CREDITS_TUNE("doubletrouble", true),
	NEW_GAME("04-welcome-to-the-world-of-pokemon", true),
	
	// Area Tunes
	PLAYER_HOUSE("05-chat-with-mom", true),
	DFS_TOWN("84-pallet-town", true),
	POKE_LAB("06-pokemon-lab", true),
	EDGE_1("83-route-1", true),
	POKE_CENTER("08-pokemon-center", true),
	TRANSITION_BUILDING("62-radio-places-people", true),
	RSA_TOWN("76-lavander-town", true),
	BLOOM_FILTER_MEADOW("45-national-park", true),
	TOM_TOWN("69-vermilion-city", true),
	POKE_GYM("26-pokemon-gym", true),
	DEADLOCK_THEME("55-rocket-hideout", true),
	
	// Battley things
	WILD_POKEMON_BATTLE("09-wild-pokemon-battle-theme", true),
	WILD_POKEMON_DEFEATED("10-wild-pokemon-defeated", true),
	TRAINER_SPOTTED("21-trainer-challenge-theme-1", true),
	TRAINER_BATTLE("22-trainer-battle-theme-1", true),
	TRAINER_DEFEATED("23-trainer-defeated", true),
	
	// Sound effects
	POKE_CENTER_HEAL("dundundundundun", false),
	LEVEL_UP("Pokemon-Level-Up-Notification", false);
	
	private String soundTitle;
	private boolean isMusic;
	
	private SoundTitle(String soundTitle, boolean isMusic)
	{
		this.soundTitle = soundTitle;
		this.isMusic = isMusic;
	}
	
	public String getSoundTitle()
	{
		return this.soundTitle;
	}
	
	public boolean isMusic()
	{
		return isMusic;
	}
}
