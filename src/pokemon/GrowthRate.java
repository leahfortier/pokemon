package pokemon;

import java.io.Serializable;

import main.Global;

public enum GrowthRate implements Serializable 
{
	FAST("Fast", new GetExperience() {
		public int getEXP(int level) {
			return (int)(.8*Math.pow(level, 3));
		}
	}), 
	MEDIUM_FAST("Medium Fast", new GetExperience() {
		public int getEXP(int level) {
			return (int)Math.pow(level, 3);
		}
	}), 
	MEDIUM_SLOW("Medium Slow", new GetExperience() {
		public int getEXP(int level) {
			return (int)(1.2*Math.pow(level, 3) - 15*level*level + 100*level - 140);
		}
	}), 
	SLOW("Slow", new GetExperience() {
		public int getEXP(int level) {
			return (int)(1.25*Math.pow(level, 3));
		}
	}), 
	ERRATIC("Erratic", new GetExperience() {
		public int getEXP(int level) {
			if (level <= 50) return (int)(.02*Math.pow(level, 3)*(100 - level));
			if (level <= 68) return (int)(.01*Math.pow(level, 3)*(150 - level));
			if (level <= 98) return (int)(Math.pow(level, 3)*((1911 - 10*level)/3.0));
			return (int)(.01*Math.pow(level, 3)*(160 - level));
		}
	}), 
	FLUCTUATING("Fluctuating", new GetExperience() {
		public int getEXP(int level) {
			if (level <= 15) return (int)(.02*Math.pow(level, 3)*(((level + 1)/3.0) + 24));
			if (level <= 36) return (int)(.02*Math.pow(level, 3)*(level + 14));
			return (int)(.02*Math.pow(level, 3)*(level/2.0 + 32));
		}
	});
	
	private final String name;
	private final GetExperience expGetter;
	
	private GrowthRate(String name, GetExperience expGetter)
	{
		this.name = name;
		this.expGetter = expGetter;
	}
	
	public int getEXP(int level)
	{
		return this.expGetter.getEXP(level);
	}
	
	private static interface GetExperience
	{
		public int getEXP(int level);
	}
	
	public static GrowthRate getRate(String rate)
	{
		for (GrowthRate growthRate : GrowthRate.values())
		{
			if (growthRate.name.equals(rate))
			{
				return growthRate;
			}
		}
		
		Global.error("Invalid Growth Rate " + rate);
		return null;
	}
}
