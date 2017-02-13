package map;

import map.weather.WeatherState;
import sound.MusicCondition;
import sound.SoundTitle;
import util.StringUtils;

public class AreaData {
	static final AreaData VOID = new AreaData(
			"Void",
			0,
			null,
			TerrainType.CAVE,
			WeatherState.NORMAL,
			SoundTitle.DEFAULT_TUNE,
			new MusicCondition[0]
	);

	private final String name;
	private final int color;

	private final String flyLocation;
	private final TerrainType terrainType;
	private final WeatherState weather;

	private final SoundTitle music;
	private final MusicCondition[] musicConditions;

	public AreaData(String name,
					int color,
					String flyLocation,
					TerrainType terrainType,
					WeatherState weather,
					SoundTitle music,
					MusicCondition[] musicConditions) {
		this.name = name;
		this.color = color;

		this.flyLocation = flyLocation;
		this.terrainType = terrainType;
		this.weather = weather;

		this.music = music;
		this.musicConditions = musicConditions;
	}

	public boolean isColor(int color) {
		return this.color == color;
	}

	public boolean isFlyLocation() {
		return !StringUtils.isNullOrEmpty(flyLocation);
	}

	public String getFlyLocation() {
		return flyLocation;
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
