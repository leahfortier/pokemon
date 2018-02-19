package mapMaker.dialogs.action.panel;

import gui.view.ViewMode;
import main.Global;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.EnumActionMatcher;
import pattern.action.EnumActionMatcher.BadgeActionMatcher;
import pattern.action.EnumActionMatcher.ChangeViewActionMatcher;
import pattern.action.EnumActionMatcher.MedalCountActionMatcher;
import pattern.action.EnumActionMatcher.SoundActionMatcher;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;
import util.GUIUtils;

import javax.swing.JComboBox;

public class EnumActionPanel<T extends Enum> extends ActionPanel {
    private final JComboBox<T> combobBox; // Not a typo

    public EnumActionPanel(String label, T[] values) {
        this.combobBox = GUIUtils.createComboBox(values);

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createComboBoxComponent(label, this.combobBox)
        );
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        T enumValue = (T)this.combobBox.getSelectedItem();
        switch (actionType) {
            case BADGE:
                return new BadgeActionMatcher((Badge)enumValue);
            case CHANGE_VIEW:
                return new ChangeViewActionMatcher((ViewMode)enumValue);
            case MEDAL_COUNT:
                return new MedalCountActionMatcher((MedalTheme)enumValue);
            case SOUND:
                return new SoundActionMatcher((SoundTitle)enumValue);
            default:
                Global.info("Invalid action type for enum panel " + actionType);
                return null;
        }
    }

    @Override
    protected void load(ActionMatcher matcher) {
        EnumActionMatcher<T> enumMatcher = (EnumActionMatcher<T>)matcher;
        this.combobBox.setSelectedItem(enumMatcher.getEnumValue());
    }
}
