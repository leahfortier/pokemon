package battle.effect;

import main.Type;

public interface AdvantageChanger
{
	public Type[] getAdvantageChange(Type attacking, Type[] defending);
}
