package map;

import sound.SoundTitle;

public class AreaData {
	public static final AreaData VOID = new AreaData("Void", 0, TerrainType.CAVE, SoundTitle.DEFAULT_TUNE, WeatherState.NORMAL);

	public enum WeatherState {
		NORMAL,
		SUN,
		RAIN,
		FOG,
		SNOW
	}

	private String name;
	private int color;

	private TerrainType terrainType;
	private WeatherState weather;
	private SoundTitle music;

	private String musicCondition; // TODO

	public AreaData(String name, int color, TerrainType terrainType, SoundTitle music, WeatherState weather) {
		this.name = name;
		this.color = color;

		this.terrainType = terrainType;
		this.weather = weather;
		this.music = music;
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
		// TODO: Condition or something
		return this.music;
	}
}
