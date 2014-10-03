package battle.effect;

import main.Type;

public interface AdvantageMultiplier
{
	public double multiplyAdvantage(Type moveType, Type[] defendingType);
}
