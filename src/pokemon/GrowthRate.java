package pokemon;

import java.io.Serializable;

import main.Global;

public enum GrowthRate implements Serializable 
{
	FAST, MEDIUM_FAST, MEDIUM_SLOW, SLOW, ERRATIC, FLUCTUATING;
	
	public static int getEXP(GrowthRate growthRate, int level)
	{
		switch (growthRate)
		{
			case FAST:
				return (int)(.8*Math.pow(level, 3));
			case MEDIUM_FAST:
				return (int)Math.pow(level, 3);
			case MEDIUM_SLOW:
				return (int)(1.2*Math.pow(level, 3) - 15*level*level + 100*level - 140);
			case SLOW:
				return (int)(1.25*Math.pow(level, 3));
			case ERRATIC:
				if (level <= 50) return (int)(.02*Math.pow(level, 3)*(100 - level));
				if (level <= 68) return (int)(.01*Math.pow(level, 3)*(150 - level));
				if (level <= 98) return (int)(Math.pow(level, 3)*((1911 - 10*level)/3.0));
				return (int)(.01*Math.pow(level, 3)*(160 - level));
			case FLUCTUATING:
				if (level <= 15) return (int)(.02*Math.pow(level, 3)*(((level + 1)/3.0) + 24));
				if (level <= 36) return (int)(.02*Math.pow(level, 3)*(level + 14));
				return (int)(.02*Math.pow(level, 3)*(level/2.0 + 32));
			default:
				Global.error("SOMETHING WENT FUCKING CRAZY WITH DEM GROWTHRATES");
				return -1; // UNNECCESSARY, JAVA
		}
	}
	
	public static GrowthRate getRate(String rate)
	{
		switch (rate)
		{
			case "Fast":
				return FAST;
			case "Medium Fast":
				return MEDIUM_FAST;
			case "Medium Slow":
				return MEDIUM_SLOW;
			case "Slow":
				return SLOW;
			case "Erratic":
				return ERRATIC;
			case "Fluctuating":
				return FLUCTUATING;
			default:
				Global.error("Invalid Growth Rate " + rate);
				return null;
		}
	}
}
