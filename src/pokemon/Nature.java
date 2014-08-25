package pokemon;

import java.awt.Color;
import java.io.Serializable;

public class Nature implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String[][] natures = {{"", "", "", "", "", ""},
		{"", "Hardy",  "Lonely", "Adamant", "Naughty", "Brave"},
		{"", "Bold",   "Docile", "Impish",  "Lax",     "Relaxed"},
		{"", "Modest", "Mild",   "Bashful", "Rash",    "Quiet"},
		{"", "Calm",   "Gentle", "Careful", "Quirky",  "Sassy"},
		{"", "Timid",  "Hasty",  "Jolly",   "Naive",   "Serious"}};
	
	private int beneficial;
	private int hindering;
	private String name;
	
	public Nature()
	{
		beneficial = (int)(Math.random()*5 + 1);
		hindering = (int)(Math.random()*5 + 1);
		name = natures[beneficial][hindering];
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getNatureVal(int stat)
	{
		if (beneficial == hindering)
		{
			return 1;
		}
		else if (beneficial == stat)
		{
			return 1.1;
		}
		else if (hindering == stat)
		{
			return .9;
		}
		return 1;
	}
	
	public Color getColor(int statIndex)
	{
		if (beneficial == hindering)
		{
			return Color.BLACK;
		}
		else if (beneficial == statIndex)
		{
			return new Color(0, 190, 0);
		}
		else if (hindering == statIndex)
		{
			return new Color(200, 0, 0);
		}
		return Color.BLACK;
	}
}
