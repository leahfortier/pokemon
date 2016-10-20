package main;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import battle.MoveType;
import pokemon.ActivePokemon;
import battle.Battle;
import battle.effect.AdvantageChanger;
import battle.effect.AdvantageMultiplier;

public enum Type implements Serializable 
{
	NORMAL(0, "Normal", new Color(230, 230, 250), -1, 0x4b), 
	FIRE(1, "Fire", new Color(220, 20, 20), 8, 0x45), 
	WATER(2, "Water", new Color(65, 105, 225), 9, 0x50), // new Color(35, 120, 220)
	ELECTRIC(3, "Electric", new Color(255, 215, 0), 11, 0x43), 
	GRASS(4, "Grass", new Color(0, 200, 0), 10, 0x48), 
	ICE(5, "Ice", new Color(5, 235, 235), 13, 0x4a), 
	FIGHTING(6, "Fighting", new Color(165, 82, 57), 0, 0x44), 
	POISON(7, "Poison", new Color(150, 30, 160), 2, 0x4c), 
	GROUND(8, "Ground", new Color(190, 170, 120), 3, 0x49), 
	FLYING(9, "Flying", new Color(135, 206, 250), 1, 0x46), 
	PSYCHIC(10, "Psychic", new Color(218, 112, 214), 12, 0x4d), 
	BUG(11, "Bug", new Color(153, 235, 27), 5, 0x40), 
	ROCK(12, "Rock", new Color(169, 135, 70), 4, 0x4e), 
	GHOST(13, "Ghost", new Color(92, 61, 139), 6, 0x47), 
	DRAGON(14, "Dragon", new Color(106, 90, 205), 14, 0x42), 
	DARK(15, "Dark", new Color(49, 79, 79), 15, 0x41), 
	STEEL(16, "Steel", new Color(200, 200, 210), 7, 0x4f),
	FAIRY(17, "Fairy", new Color(221, 160, 221), -1, 0x52),
	NONE(18, "None", Color.WHITE, -1, 0x51);
	
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

	public static boolean blockAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
		if (defending.isType(b, Type.GRASS) && attacking.getAttack().isMoveType(MoveType.POWDER)) {
			b.addMessage(defending.getName() + " is immune to Powder moves!");
			return true;
		}
		
		return false;
	}
	
	public static double getAdvantage(ActivePokemon attacking, ActivePokemon defending, Battle b) {
		Type moveType = attacking.getAttackType(); 
		
		// Check the defending Pokemon's effects and held item as well as the attacking Pokemon's ability for advantage changes 
		List<Object> invokees = new ArrayList<>();
		invokees.addAll(defending.getEffects());
		invokees.add(defending.getHeldItem(b));
		invokees.add(attacking.getAbility());
		
		Type[] originalType = defending.getType(b);
		Type[] defendingType = (Type[])Battle.updateInvoke(1, invokees.toArray(), AdvantageChanger.class, "getAdvantageChange", moveType, originalType.clone());
		
		// If nothing was updated, do special case check stupid things for fucking levitation which fucks everything up
		if (defendingType[0] == originalType[0] && defendingType[1] == originalType[1] && moveType == GROUND) {
			// Pokemon that are levitating cannot be hit by ground type moves
			if (defending.isLevitating(b)) {
				return 0;
			}
			
			// If the Pokemon is not levitating due to some effect and is flying type, ground moves should hit
			for (int i = 0; i < 2; i++) {
				if (defendingType[i] == FLYING) {
					defendingType[i] = NONE;
				}
			}
		}
		
		// Get the advantage and apply any multiplier that may come from the attack
		double adv = getBasicAdvantage(moveType, defendingType[0])*getBasicAdvantage(moveType, defendingType[1]);
		adv = Battle.multiplyInvoke(adv, new Object[] {attacking.getAttack()}, AdvantageMultiplier.class, "multiplyAdvantage", moveType, defendingType);	
		
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
			return p.hasAbility(Namesies.ADAPTABILITY_ABILITY) ? 2 : 1.5;
		}
		
		return 1; 
	}

	private int index;
	private String name;
	private Color color;
	private int hiddenIndex;
	private int imageIndex;

	Type(int index, String name, Color color, int hiddenIndex, int imageIndex) {
		this.index = index;
		this.name = name;
		this.color = color;
		this.hiddenIndex = hiddenIndex;
		this.imageIndex = imageIndex;
	}

	public int getIndex() {
		return this.index;
	}

	public String getName() {
		return this.name;
	}

	public Color getColor() {
		return this.color;
	}
	
	public int getImageIndex() {
		return this.imageIndex;
	}

	public Color getTextColor() {
		if (this == NORMAL) {
			return new Color(180, 180, 200);
		} 
		else if (this == STEEL) {
			return new Color(160, 160, 170);
		}
		
		return color;
	}
	
	public static Color[] getColors(Type[] t) {
		return new Color[] {t[0].getColor(), t[t[1] == Type.NONE ? 0 : 1].getColor()};
	}
	
	public static Color[] getColors(ActivePokemon p) {
		return getColors(p.isEgg() ? new Type[] { Type.NORMAL, Type.NONE} : p.getActualType());
	}
	
	public static Type getHiddenType(int hiddenIndex) {
		for (Type t : values()) {
			if (t.hiddenIndex == hiddenIndex) {
				return t;
			}
		}

		Global.error("Invalid hidden type index " + hiddenIndex);
		return null;
	}
}
