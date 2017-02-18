package map;

import map.weather.WeatherState;
import sound.MusicCondition;
import sound.SoundTitle;
import util.StringUtils;

import java.awt.Color;

public class AreaData {
	public static final AreaData VOID = new AreaData(
			"Void",
			Color.BLACK,
			null,
			TerrainType.CAVE,
			WeatherState.NORMAL,
			SoundTitle.DEFAULT_TUNE,
			new MusicCondition[0]
	);

	private final String name;
	private final Color color;

	private final String flyLocation;
	private final TerrainType terrainType;
	private final WeatherState weather;

	private final SoundTitle music;
	private final MusicCondition[] musicConditions;

	public AreaData(String name,
					Color color,
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

	public boolean hasColor() {
		return !this.color.equals(Color.BLACK);
	}

	public boolean isColor(Color color) {
		return this.color.equals(color);
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
