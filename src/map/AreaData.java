package map;

import sound.MusicCondition;
import sound.SoundTitle;

public class AreaData {
	static final AreaData VOID = new AreaData(
			"Void",
			0,
			TerrainType.CAVE,
			WeatherState.NORMAL,
			SoundTitle.DEFAULT_TUNE,
			new MusicCondition[0]
	);

	public enum WeatherState {
		NORMAL,
		SUN,
		RAIN,
		FOG,
		SNOW
	}

	private final String name;
	private final int color;

	private final TerrainType terrainType;
	private final WeatherState weather;

	private final SoundTitle music;
	private final MusicCondition[] musicConditions;

	public AreaData(String name,
					int color,
					TerrainType terrainType,
					WeatherState weather,
					SoundTitle music,
					MusicCondition[] musicConditions) {
		this.name = name;
		this.color = color;

		this.terrainType = terrainType;
		this.weather = weather;

		this.music = music;
		this.musicConditions = musicConditions;
	}

	public boolean isColor(int color) {
		return this.color == color;
	}

	public WeatherState getWeather() {
		return weather;
	}

	public TerrainType getTerrain() {
		return terrainType;
	}

	public String getAreaName() {
		return name;
	}

	public SoundTitle getMusic() {
		for (MusicCondition condition : musicConditions) {
			if (condition.isTrue()) {
				return condition.getMusic();
			}
		}

		return this.music;
	}
}
