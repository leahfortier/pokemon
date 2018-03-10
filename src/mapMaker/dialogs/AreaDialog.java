package mapMaker.dialogs;

import map.overworld.TerrainType;
import map.weather.WeatherState;
import pattern.map.AreaMatcher;
import sound.SoundTitle;
import util.GuiUtils;

import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AreaDialog extends TriggerDialog<AreaMatcher> {

    private final JTextField displayNameTextField;
    private final JTextField flyLocationTextField;
    private final JComboBox<TerrainType> terrainComboBox;
    private final JComboBox<WeatherState> weatherComboBox;
    private final JComboBox<SoundTitle> musicComboBox;

    public AreaDialog(AreaMatcher areaMatcher) {
        super("Area Editor");

        displayNameTextField = GuiUtils.createTextField();
        flyLocationTextField = GuiUtils.createTextField();

        terrainComboBox = GuiUtils.createComboBox(TerrainType.values());
        terrainComboBox.setSelectedItem(TerrainType.BUILDING);

        weatherComboBox = GuiUtils.createComboBox(WeatherState.values());
        weatherComboBox.setSelectedItem(WeatherState.NORMAL);

        musicComboBox = GuiUtils.createComboBox(SoundTitle.values());
        musicComboBox.setSelectedItem(SoundTitle.DEFAULT_TUNE);

        GuiUtils.setVerticalLayout(
                this,
                GuiUtils.createTextFieldComponent("Display Name", displayNameTextField),
                GuiUtils.createTextFieldComponent("Fly Location", flyLocationTextField),
                GuiUtils.createComboBoxComponent("Terrain", terrainComboBox),
                GuiUtils.createComboBoxComponent("Weather", weatherComboBox),
                GuiUtils.createComboBoxComponent("Music", musicComboBox)
        );

        this.load(areaMatcher);
    }

    @Override
    protected AreaMatcher getMatcher() {
        return new AreaMatcher(
                displayNameTextField.getText(),
                flyLocationTextField.getText(),
                (TerrainType)terrainComboBox.getSelectedItem(),
                (WeatherState)weatherComboBox.getSelectedItem(),
                (SoundTitle)musicComboBox.getSelectedItem(),
                null // TODO: Music conditions
        );
    }

    private void load(AreaMatcher matcher) {
        if (matcher == null) {
            return;
        }

        displayNameTextField.setText(matcher.getDisplayName());
        flyLocationTextField.setText(matcher.getFlyLocation());
        terrainComboBox.setSelectedItem(matcher.getTerrain());
        weatherComboBox.setSelectedItem(matcher.getWeather());
        musicComboBox.setSelectedItem(matcher.getMusic());
    }
}
