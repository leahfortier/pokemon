package battle.effect;

import main.Type;

public interface AdvantageChanger {
	Type[] getAdvantageChange(Type attacking, Type[] defending);
}
