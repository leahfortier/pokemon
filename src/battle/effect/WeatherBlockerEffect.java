package battle.effect;

import battle.effect.Weather.WeatherType;

public interface WeatherBlockerEffect
{
	public boolean block(WeatherType weather);
}
