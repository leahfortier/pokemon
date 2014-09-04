package item.hold;

import pokemon.Stat;
import battle.effect.StatChangingEffect;

public interface PowerItem extends HoldItem, StatChangingEffect
{
	public int[] getEVs(int[] vals);
	public Stat toIncrease();
}
