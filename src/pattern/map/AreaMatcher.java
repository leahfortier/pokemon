package pattern.map;

import map.AreaData;
import map.TerrainType;
import map.weather.WeatherState;
import sound.MusicCondition;
import sound.SoundTitle;
import util.StringUtils;

public class AreaMatcher {
    private String color;
    private String displayName;
    private TerrainType terrain;
    private WeatherState weather;
    private SoundTitle music;
    private MusicConditionMatcher[] musicConditions;

    private transient AreaData areaData;

    public boolean hasColor() {
        return !StringUtils.isNullOrEmpty(color);
    }

    private int getColor() {
        return StringUtils.isNullOrEmpty(this.color) ? 0 : (int) Long.parseLong(this.color, 16);
    }

    private WeatherState getWeather() {
        return this.weather == null ? WeatherState.NORMAL : this.weather;
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

        areaData = new AreaData(this.displayName, this.getColor(), this.terrain, this.getWeather(), this.music, this.getMusicConditions());
        return areaData;
    }

    private static class MusicConditionMatcher {
        private String condition;
        private SoundTitle music;
    }
}
