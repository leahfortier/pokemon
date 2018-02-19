package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionPanel;
import pattern.action.EnumActionMatcher;
import util.GUIUtils;

import javax.swing.JComboBox;
import java.util.function.Function;

public class EnumActionPanel<T extends Enum> extends ActionPanel<EnumActionMatcher<T>> {
    private final JComboBox<T> combobBox; // Not a typo

    private final Function<T, EnumActionMatcher<T>> actionMatcherGetter;

    public EnumActionPanel(String label, T[] values, Function<T, EnumActionMatcher<T>> actionMatcherGetter) {
        this.actionMatcherGetter = actionMatcherGetter;

        this.combobBox = GUIUtils.createComboBox(values);
        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createComboBoxComponent(label, this.combobBox)
        );
    }

    @Override
    public EnumActionMatcher<T> getActionMatcher() {
        T enumValue = (T)this.combobBox.getSelectedItem();
        return this.actionMatcherGetter.apply(enumValue);
    }

    @Override
    protected void load(EnumActionMatcher<T> matcher) {
        this.combobBox.setSelectedItem(matcher.getEnumValue());
    }
}
