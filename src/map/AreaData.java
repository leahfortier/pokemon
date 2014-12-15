package map;

import gui.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.Stat;
import battle.Attack;
import battle.effect.Effect;
import battle.effect.PokemonEffect;
import battle.effect.Status.StatusCondition;

public class AreaData
{

	public static final Pattern areaSoundConditionPattern = Pattern.compile("(?:([()&|!\\w]+)\\s*:\\s*)?([\\w-]+)");

	public enum WeatherState
	{
		NORMAL, SUN, RAIN, FOG, SNOW
	};

	public enum TerrainType
	{
		GRASS(Type.GRASS, Namesies.ENERGY_BALL_ATTACK, StatusCondition.ASLEEP),
		BUILDING(Type.NORMAL, Namesies.TRI_ATTACK_ATTACK, StatusCondition.PARALYZED),
		CAVE(Type.ROCK, Namesies.POWER_GEM_ATTACK, Namesies.FLINCH_EFFECT),
		SAND(Type.GROUND, Namesies.EARTH_POWER_ATTACK, Stat.ACCURACY), 
		WATER(Type.WATER, Namesies.HYDRO_PUMP_ATTACK, Stat.ATTACK),
		SNOW(Type.ICE, Namesies.FROST_BREATH_ATTACK, StatusCondition.FROZEN), 
		ICE(Type.ICE, Namesies.ICE_BEAM_ATTACK, StatusCondition.FROZEN),
		MISTY(Type.FAIRY, Namesies.MOONBLAST_ATTACK, Stat.SP_ATTACK),
		ELECTRIC(Type.ELECTRIC, Namesies.THUNDERBOLT_ATTACK, StatusCondition.PARALYZED);
		
		private Type type;
		private Attack attack;
		
		private StatusCondition status;
		private int[] statChanges;
		private List<Effect> effects;
		
		private int backgroundIndex;
		private int playerCircleIndex;
		private int opponentCircleIndex;
		
		private TerrainType(Type type, Namesies attack, Object effect)
		{
			this.type = type;
			this.attack = Attack.getAttack(attack);
		
			this.status = StatusCondition.NONE;
			this.statChanges = new int[Stat.NUM_BATTLE_STATS];
			this.effects = new ArrayList<>();
			
			if (effect instanceof StatusCondition)
			{
				this.status = (StatusCondition)effect;
			}
			else if (effect instanceof Stat)
			{
				this.statChanges[((Stat)effect).index()] = -1;
			}
			else if (effect instanceof Namesies)
			{
				this.effects.add(PokemonEffect.getEffect((Namesies)effect));
			}
			else
			{
				Global.error("Invalid effect for terrain type " + this.name());
			}
			
			this.backgroundIndex = 0x100 + this.ordinal();
			this.playerCircleIndex = 0x200 + this.ordinal();
			this.opponentCircleIndex = 0x300 + this.ordinal();
		}
		
		static
		{
			// TODO: Need Terrain images for misty and electric terrain -- use snow and sand for now (for no particular reason)
			MISTY.backgroundIndex = SNOW.backgroundIndex;
			ELECTRIC.backgroundIndex = SAND.backgroundIndex;
			
			MISTY.playerCircleIndex = SNOW.playerCircleIndex;
			ELECTRIC.playerCircleIndex = SAND.playerCircleIndex;
			
			MISTY.opponentCircleIndex = SNOW.opponentCircleIndex;
			ELECTRIC.opponentCircleIndex = SAND.opponentCircleIndex;
		}
		
		public Type getType()
		{
			return type;
		}
		
		public Attack getAttack()
		{
			return attack;
		}
		
		public StatusCondition getStatusCondition()
		{
			return status;
		}
		
		public int[] getStatChanges()
		{
			return statChanges;
		}
		
		public List<Effect> getEffects()
		{
			return effects;
		}
		
		public int getBackgroundIndex()
		{
			return backgroundIndex;
		}
		
		public int getPlayerCircleIndex()
		{
			return playerCircleIndex;
		}
		
		public int getOpponentCircleIndex()
		{
			return opponentCircleIndex;
		}
	};

	private String name;
	private int color;
	private TerrainType terrainType;
	private WeatherState weather;

	private String musicCondition;
	private String musicTriggerName;

	public AreaData(String name, int color, String weather, String terrainType, String musicCondition)
	{
		this.name = name;
		this.color = color;
		this.terrainType = TerrainType.valueOf(terrainType);

		this.musicCondition = musicCondition;

		this.weather = WeatherState.valueOf(weather);
	}

	public void addMusicTriggers(GameData data)
	{
		if (musicCondition != null)
		{
			StringBuilder groupTriggers = new StringBuilder();
			String areaNameDisplay = name.replace(' ', '_').replaceAll("\\W", "");

			Matcher areaSoundMatcher = areaSoundConditionPattern.matcher(musicCondition);
			while (areaSoundMatcher.find())
			{
				String condition = areaSoundMatcher.group(1);
				String musicName = areaSoundMatcher.group(2);
				String soundTriggerName = "SoundTrigger_AreaSound_for_" + areaNameDisplay + "_MusicName_" + musicName;

				// System.out.println(condition + " : " + musicName);

				data.addTrigger("Sound", soundTriggerName, (condition != null ? "condition: " + condition : "") + "\nmusicName: " + musicName);
				groupTriggers.append("trigger: " + soundTriggerName + "\n");
			}

			data.addTrigger("Group", "GroupTrigger_AreaSound_for_" + areaNameDisplay, groupTriggers.toString());
			musicTriggerName = "GroupTrigger_AreaSound_for_" + areaNameDisplay;
		}
	}

	public WeatherState getWeather()
	{
		return weather;
	}

	public TerrainType getTerrain()
	{
		return terrainType;
	}

	public String getAreaName()
	{
		return name;
	}

	public String getMusicTriggerName()
	{
		return musicTriggerName;
	}
}
