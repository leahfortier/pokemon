package main;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pokemon.ActivePokemon;
import battle.Battle;
import battle.effect.AdvantageChanger;

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
	NONE(17, "None", Color.WHITE, -1, 0);
	
	private static final double typeAdvantage[][] = {
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, .5,  0,  1,  1, .5, 1}, // Normal
		{1, .5, .5,  1,  2,  2,  1,  1,  1,  1,  1,  2, .5,  1, .5,  1,  2, 1}, // Fire
		{1,  2, .5,  1, .5,  1,  1,  1,  2,  1,  1,  1,  2,  1, .5,  1,  1, 1}, // Water
		{1,  1,  2, .5, .5,  1,  1,  1,  0,  2,  1,  1,  1,  1, .5,  1,  1, 1}, // Electric
		{1, .5,  2,  1, .5,  1,  1, .5,  2, .5,  1, .5,  2,  1, .5,  1, .5, 1}, // Grass
		{1, .5, .5,  1,  2, .5,  1,  1,  2,  2,  1,  1,  1,  1,  2,  1, .5, 1}, // Ice
		{2,  1,  1,  1,  1,  2,  1, .5,  1, .5, .5, .5,  2,  0,  1,  2,  2, 1}, // Fighting
		{1,  1,  1,  1,  2,  1,  1, .5, .5,  1,  1,  1, .5, .5,  1,  1,  0, 1}, // Poison
		{1,  2,  1,  2, .5,  1,  1,  2,  1,  0,  1, .5,  2,  1,  1,  1,  2, 1}, // Ground
		{1,  1,  1, .5,  2,  1,  2,  1,  1,  1,  1,  2, .5,  1,  1,  1, .5, 1}, // Flying
		{1,  1,  1,  1,  1,  1,  2,  2,  1,  1, .5,  1,  1,  1,  1,  0, .5, 1}, // Psychic
		{1, .5,  1,  1,  2,  1, .5, .5,  1, .5,  2,  1,  1, .5,  1,  2, .5, 1}, // Bug
		{1,  2,  1,  1,  1,  2, .5,  1, .5,  2,  1,  2,  1,  1,  1,  1, .5, 1}, // Rock
		{0,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1,  1,  2,  1, .5, .5, 1}, // Ghost
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  1, .5, 1}, // Dragon
		{1,  1,  1,  1,  1,  1, .5,  1,  1,  1,  2,  1,  1,  2,  1, .5, .5, 1}, // Dark
		{1, .5, .5, .5,  1,  2,  1,  1,  1,  1,  1,  1,  2,  1,  1,  1, .5, 1}, // Steel
		{1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 1}}; // No Type
	
	public static double getAdvantage(Type moveType, ActivePokemon p, Battle b)
	{
		// Check the defending Pokemon's effects and held item as well as the attacking Pokemon's ability for advantage changes 
		List<Object> invokees = new ArrayList<>();
		invokees.addAll(p.getEffects());
		invokees.add(p.getHeldItem(b));
		invokees.add(b.getOtherPokemon(p.user()).getAbility());
		
		Type[] pType = p.getType(b);
		Type[] defending = (Type[])Global.updateInvoke(1, invokees.toArray(), AdvantageChanger.class, "getAdvantageChange", moveType, new Type[] {pType[0], pType[1]});
		
		// If nothing was updated, do special case check stupid things for fucking levitation which fucks everything up
		if (defending[0] == pType[0] && defending[1] == pType[1] && moveType == GROUND)
		{
			// Pokemon that are levitating cannot be hit by ground type moves
			if (p.isLevitating(b)) 
			{
				return 0;
			}
			
			// If the Pokemon is not levitating due to some effect and is flying type, ground moves should hit
			for (int i = 0; i < 2; i++)
			{
				if (defending[i] == FLYING)
				{
					defending[i] = NONE;
				}
			}
		}
		
		int index = moveType.index;
		int type1 = defending[0].index;
		int type2 = defending[1].index;
		
		return typeAdvantage[index][type1]*typeAdvantage[index][type2];
	}
	
	public static double getAdvantage(Type attacking, Type defending)
	{
		return typeAdvantage[attacking.index][defending.index];
	}
	
	public static double getSTAB(Battle b, ActivePokemon p)
	{
		Type[] pokemonType = p.getType(b);
		Type attackType = p.getAttack().getType(b, p);
		
		return pokemonType[0] ==  attackType|| pokemonType[1] == attackType ? (p.hasAbility(Namesies.ADAPTABILITY_ABILITY) ? 2 : 1.5) : 1; 
	}

	private int index;
	private String name;
	private Color color;
	private int hiddenIndex;
	private int imageIndex;

	private Type(int i, String n, Color c, int h, int img)
	{
		index = i;
		name = n;
		color = c;
		hiddenIndex = h;
		imageIndex = img;
	}

	public int getIndex()
	{
		return index;
	}

	public String getName()
	{
		return name;
	}

	public Color getColor()
	{
		return color;
	}
	
	public int getImageIndex()
	{
		return imageIndex;
	}

	public Color getTextColor()
	{
		if (this == NORMAL)
		{
			return new Color(180, 180, 200);
		} 
		else if (this == STEEL)
		{
			return new Color(160, 160, 170);
		}
		
		return color;
	}
	
	public static Color[] getColors(Type[] t)
	{
		return new Color[] {t[0].getColor(), t[t[1] == Type.NONE ? 0 : 1].getColor()};
	}
	
	public static Color[] getColors(ActivePokemon p)
	{
		return getColors(p.isEgg() ? new Type[] { Type.NORMAL, Type.NONE} : p.getActualType());
	}
	
	public static Type getHiddenType(int hiddenIndex)
	{
		for (Type t : values())
			if (t.hiddenIndex == hiddenIndex) 
				return t;
		
		Global.error("Invalid hidden type index " + hiddenIndex);
		return null;
	}
}
