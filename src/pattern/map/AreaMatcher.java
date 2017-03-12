package pattern.map;

import map.area.AreaData;
import map.area.MusicCondition;
import map.overworld.TerrainType;
import map.weather.WeatherState;
import sound.SoundTitle;

import java.awt.Color;

public class AreaMatcher {
    private Integer color;
    private String displayName;
    private String flyLocation;
    private TerrainType terrain;
    private WeatherState weather;
    private SoundTitle music;
    private MusicConditionMatcher[] musicConditions;

    private transient AreaData areaData;

    public AreaMatcher(String displayName,
                       String flyLocation,
                       TerrainType terrain,
                       WeatherState weather,
                       SoundTitle music,
                       MusicConditionMatcher[] musicConditions
    ) {
        this.displayName = displayName;
        this.flyLocation = flyLocation;
        this.terrain = terrain;
        this.weather = weather;
        this.music = music;
        this.musicConditions = musicConditions;
    }

    public boolean hasColor() {
        return color != null && color != 0;
    }

    public Color getColor() {
        return this.hasColor() ? new Color(this.color) : Color.BLACK;
    }

    public void setColor(Color color) {
        this.color = color.getRGB();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFlyLocation() {
        return this.flyLocation;
    }

    public TerrainType getTerrain() {
        return this.terrain == null ? TerrainType.BUILDING : this.terrain;
    }

    public WeatherState getWeather() {
        return this.weather == null ? WeatherState.NORMAL : this.weather;
    }

    public SoundTitle getMusic() {
        return this.music == null ? SoundTitle.DEFAULT_TUNE : this.music;
    }

    private MusicCondition[] getMusicConditions() {
        if (this.musicConditions == null) {
            return new MusicCondition[0];
        }

        MusicCondition[] musicConditions = new MusicCondition[this.musicConditions.length];
        for (int i = 0; i < this.musicConditions.length; i++) {
            musicConditions[i] = new MusicCondition(this.musicConditions[i].music, this.musicConditions[i].condition);
        }

        return musicConditions;
    }

    public AreaData getAreaData() {
        if (areaData != null) {
            return areaData;
        }

        areaData = new AreaData(
                this.displayName,
                this.getColor(),
                this.flyLocation,
                this.terrain,
                this.getWeather(),
                this.music,
                this.getMusicConditions()
        );
        return areaData;
    }

    private static class MusicConditionMatcher {
        private String condition;
        private SoundTitle music;
    }
}
