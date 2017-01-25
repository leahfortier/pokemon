package type;

import battle.Battle;
import battle.attack.MoveType;
import battle.effect.generic.EffectInterfaces.AdvantageChanger;
import battle.effect.generic.EffectInterfaces.AdvantageMultiplierMove;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import util.FileIO;
import util.Folder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public enum Type implements Serializable {
	NORMAL(0, "Normal", () -> TypeAdvantage.NORMAL, new Color(230, 230, 250), -1),
	FIRE(1, "Fire", () -> TypeAdvantage.FIRE, new Color(220, 20, 20), 8),
	WATER(2, "Water", () -> TypeAdvantage.WATER, new Color(35, 120, 220), 9),
	ELECTRIC(3, "Electric", () -> TypeAdvantage.ELECTRIC, new Color(255, 215, 0), 11),
	GRASS(4, "Grass", () -> TypeAdvantage.GRASS, new Color(120, 200, 80), 10),
	ICE(5, "Ice", () -> TypeAdvantage.ICE, new Color(5, 235, 235), 13),
	FIGHTING(6, "Fighting", () -> TypeAdvantage.FIGHTING, new Color(165, 82, 57), 0),
	POISON(7, "Poison", () -> TypeAdvantage.POISON, new Color(150, 30, 160), 2),
	GROUND(8, "Ground", () -> TypeAdvantage.GROUND, new Color(190, 170, 120), 3),
	FLYING(9, "Flying", () -> TypeAdvantage.FLYING, new Color(135, 206, 250), 1),
	PSYCHIC(10, "Psychic", () -> TypeAdvantage.PSYCHIC, new Color(218, 112, 214), 12),
	BUG(11, "Bug", () -> TypeAdvantage.BUG, new Color(153, 235, 27), 5),
	ROCK(12, "Rock", () -> TypeAdvantage.ROCK, new Color(169, 135, 70), 4),
	GHOST(13, "Ghost", () -> TypeAdvantage.GHOST, new Color(92, 61, 139), 6),
	DRAGON(14, "Dragon", () -> TypeAdvantage.DRAGON, new Color(106, 90, 205), 14),
	DARK(15, "Dark", () -> TypeAdvantage.DARK, new Color(49, 79, 79), 15),
	STEEL(16, "Steel", () -> TypeAdvantage.STEEL, new Color(200, 200, 210), 7),
	FAIRY(17, "Fairy", () -> TypeAdvantage.FAIRY, new Color(221, 160, 221), -1),
	NO_TYPE(18, "Unknown", () -> TypeAdvantage.NO_TYPE, new Color(255, 255, 255, 0), -1); // TODO: TYPE: NULL MUTHAFUCKA

	// TODO: This is ass do that other thingy
	private static final double typeAdvantage[][] = {
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, .5,  0,  1,  1, .5,  1, 1}, // Normal
		{1, .5, .5,  1,  2,  2,  1,  1,  1,  1,  1,  2, .5,  1, .5,  1,  2,  1, 1}, // Fire
		{1,  2, .5,  1, .5,  1,  1,  1,  2,  1,  1,  1,  2,  1, .5,  1,  1,  1, 1}, // Water
		{1,  1,  2, .5, .5,  1,  1,  1,  0,  2,  1,  1,  1,  1, .5,  1,  1,  1, 1}, // Electric
		{1, .5,  2,  1, .5,  1,  1, .5,  2, .5,  1, .5,  2,  1, .5,  1, .5,  1, 1}, // Grass
		{1, .5, .5,  1,  2, .5,  1,  1,  2,  2,  1,  1,  1,  1,  2,  1, .5,  1, 1}, // Ice
		{2,  1,  1,  1,  1,  2,  1, .5,  1, .5, .5, .5,  2,  0,  1,  2,  2, .5, 1}, // Fighting
		{1,  1,  1,  1,  2,  1,  1, .5, .5,  1,  1,  1, .5, .5,  1,  1,  0,  2, 1}, // Poison
		{1,  2,  1,  2, .5,  1,  1,  2,  1,  0,  1, .5,  2,  1,  1,  1,  2,  1, 1}, // Ground
		{1,  1,  1, .5,  2,  1,  2,  1,  1,  1,  1,  2, .5,  1,  1,  1, .5,  1, 1}, // Flying
		{1,  1,  1,  1,  1,  1,  2,  2,  1,  1, .5,  1,  1,  1,  1,  0, .5,  1, 1}, // Psychic
		{1, .5,  1,  1,  2,  1, .5, .5,  1, .5,  2,  1,  1, .5,  1,  2, .5, .5, 1}, // Bug
		{1,  2,  1,  1,  1,  2, .5,  1, .5,  2,  1,  2,  1,  1,  1,  1, .5,  1, 1}, // Rock
		{0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1,  1, 1}, // Ghost
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1, .5,  0, 1}, // Dragon
		{1,  1,  1,  1,  1,  1, .5,  1,  1,  1,  2,  1,  1,  2,  1, .5,  1, .5, 1}, // Dark
		{1, .5, .5, .5,  1,  2,  1,  1,  1,  1,  1,  1,  2,  1,  1,  1, .5,  2, 1}, // Steel
		{1, .5,  1,  1,  1,  1,  2, .5,  1,  1,  1,  1,  1,  1,  2,  2, .5,  1, 1}, // Fairy
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1}}; // No Type

	private final int index;
	private final String name;
	private final AdvantageGetter advantageGetter;
	private final Color color;
	private final int hiddenIndex;
	private final BufferedImage image;

	Type(int index, String name, AdvantageGetter advantageGetter, Color color, int hiddenIndex) {
		this.index = index;
		this.name = name;
		this.advantageGetter = advantageGetter;
		this.color = color;
		this.hiddenIndex = hiddenIndex;

		String imageName = "Type" + name;
		this.image = FileIO.readImage(Folder.TYPE_TILES + imageName);
	}

	public int getIndex() {
		return this.index;
	}

	public String getName() {
		return this.name;
	}

	public TypeAdvantage getAdvantage() {
		return this.advantageGetter.getAdvantage();
	}

	public Color getColor() {
		return this.color;
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
	
	public static Color[] getColors(Type[] t) {
		return new Color[] {t[0].getColor(), t[t[1] == Type.NO_TYPE ? 0 : 1].getColor()};
	}
	
	public static Color[] getColors(ActivePokemon p) {
		return getColors(p.isEgg() ? new Type[] { Type.NORMAL, Type.NO_TYPE } : p.getActualType());
	}
	
	public static Type getHiddenType(int hiddenIndex) {
		for (Type type : values()) {
			if (type.hiddenIndex == hiddenIndex) {
				return type;
			}
		}

		Global.error("Invalid hidden type index " + hiddenIndex);
		return null;
	}

	public static boolean blockAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
		if (defending.isType(b, Type.GRASS) && attacking.getAttack().isMoveType(MoveType.POWDER)) {
			Messages.add(new MessageUpdate(defending.getName() + " is immune to Powder moves!"));
			return true;
		}

		return false;
	}

	public static double getAdvantage(ActivePokemon attacking, ActivePokemon defending, Battle b) {
		Type moveType = attacking.getAttackType();

		Type[] originalType = defending.getType(b);
		Type[] defendingType = AdvantageChanger.updateDefendingType(b, attacking, defending, moveType, originalType.clone());

		// TODO: I hate all of this change everything
		// If nothing was updated, do special case check stupid things for fucking levitation which fucks everything up
		if (defendingType[0] == originalType[0] && defendingType[1] == originalType[1] && moveType == GROUND) {
			// Pokemon that are levitating cannot be hit by ground type moves
			if (defending.isLevitating(b, attacking)) {
				return 0;
			}

			// If the Pokemon is not levitating due to some effect and is flying type, ground moves should hit
			for (int i = 0; i < 2; i++) {
				if (defendingType[i] == FLYING) {
					defendingType[i] = NO_TYPE;
				}
			}
		}

		// Get the advantage and apply any multiplier that may come from the attack
		double adv = getBasicAdvantage(moveType, defendingType[0])*getBasicAdvantage(moveType, defendingType[1]);
		adv = AdvantageMultiplierMove.updateModifier(adv, attacking, moveType, defendingType);

		return adv;
	}

	public static double getBasicAdvantage(Type attacking, ActivePokemon defending, Battle b) {
		Type[] defendingType = defending.getType(b);
		return getBasicAdvantage(attacking, defendingType[0])*getBasicAdvantage(attacking, defendingType[1]);
	}

	public static double getBasicAdvantage(Type attacking, Type defending) {
		return typeAdvantage[attacking.index][defending.index];
	}

	public static double getSTAB(Battle b, ActivePokemon p) {
		Type[] pokemonType = p.getType(b);
		Type attackType = p.getAttackType();

		// Same type -- STAB
		if (pokemonType[0] == attackType || pokemonType[1] == attackType) {
			// The adaptability ability increases stab
			return p.hasAbility(AbilityNamesies.ADAPTABILITY) ? 2 : 1.5;
		}

		return 1;
	}

	interface AdvantageGetter {
		TypeAdvantage getAdvantage();
	}
}
