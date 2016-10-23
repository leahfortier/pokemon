package battle.effect;

import main.Type;

public interface AdvantageMultiplier {
	double multiplyAdvantage(Type moveType, Type[] defendingType);
}
