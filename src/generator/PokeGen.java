package generator;

import util.FileIO;
import main.Namesies.NamesiesType;

public class PokeGen
{
	private static final String EFFECTS_FOLDER = FileIO.makePath("src", "battle", "effect");
	private static final String POKEMON_EFFECT_PATH = EFFECTS_FOLDER + "PokemonEffect.java";
	private static final String TEAM_EFFECT_PATH = EFFECTS_FOLDER + "TeamEffect.java";
	private static final String BATTLE_EFFECT_PATH = EFFECTS_FOLDER + "BattleEffect.java";
	private static final String WEATHER_PATH = EFFECTS_FOLDER + "Weather.java";
	
	private static final String MOVE_PATH = FileIO.makePath("src", "battle") + "Attack.java";
	private static final String ABILITY_PATH = FileIO.makePath("src", "pokemon") + "Ability.java";
	private static final String ITEM_PATH = FileIO.makePath("src", "item") + "Item.java";
	
	public enum Generator {
		ATTACK_GEN("Moves.txt", MOVE_PATH, "Attack", NamesiesType.ATTACK, false, true),
		POKEMON_EFFECT_GEN("PokemonEffects.txt", POKEMON_EFFECT_PATH, "PokemonEffect", NamesiesType.EFFECT, true, true),
		TEAM_EFFECT_GEN("TeamEffects.txt", TEAM_EFFECT_PATH, "TeamEffect", NamesiesType.EFFECT, true, true),
		BATTLE_EFFECT_GEN("BattleEffects.txt", BATTLE_EFFECT_PATH, "BattleEffect", NamesiesType.EFFECT, true, true),
		WEATHER_GEN("Weather.txt", WEATHER_PATH, "Weather", NamesiesType.EFFECT, true, true),
		ABILITY_GEN("Abilities.txt", ABILITY_PATH, "Ability", NamesiesType.ABILITY, true, true),
		ITEM_GEN("Items.txt", ITEM_PATH, "Item", NamesiesType.ITEM, false, true);
		
		private final String inputPath;
		private final String outputPath;
		private final String superClass;
		private final NamesiesType appendsies;
		private final boolean activate;
		private final boolean mappity;
		
		private Generator(String inputPath, String outputPath, String superClass, NamesiesType appendsies, boolean activate, boolean mappity)
		{
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.superClass = superClass;
			this.appendsies = appendsies;
			this.activate = activate;
			this.mappity = mappity;
		}
		
		public String getInputPath() {
			return this.inputPath;
		}
		
		public String getOutputPath() {
			return this.outputPath;
		}
		
		public String getSuperClass() {
			return this.superClass;
		}
		
		public NamesiesType getNamesiesType() {
			return this.appendsies;
		}
		
		public boolean isActivate() {
			return this.activate;
		}
		
		public boolean isMappity() {
			return this.mappity;
		}	
	}
}
