package mapMaker.dialogs;

import map.condition.ConditionSet;
import pattern.generic.TriggerMatcher;
import pattern.map.ConditionMatcher;
import pattern.map.ConditionsMatcher;
import util.GUIUtils;
import util.string.StringUtils;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionPanel extends JPanel {
    private static final String NONE = "None";
    private static final String CUSTOM = "Custom";

    private final JComboBox<String> combobBox; // Not a typo
    private ConditionSet conditionSet;

    public ConditionPanel() {
        List<ConditionMatcher> conditionMatchers = ConditionsMatcher.getConditionMatchers();
        List<String> matcherNames = conditionMatchers.stream().map(ConditionMatcher::getName).collect(Collectors.toList());
        matcherNames.add(0, NONE);
        this.combobBox = GUIUtils.createComboBox(matcherNames.toArray(new String[0]));

        JPanel panel = GUIUtils.createComboBoxComponent("Condition", this.combobBox);
        GUIUtils.setVerticalLayout(this, panel);
    }

    public String getConditionName() {
        String name = (String)this.combobBox.getSelectedItem();
        if (StringUtils.isNullOrEmpty(name) || name.equals(NONE) || name.equals(CUSTOM)) {
            return null;
        }

        return name;
    }

    public ConditionSet getConditionSet() {
        String name = (String)this.combobBox.getSelectedItem();
        if (!StringUtils.isNullOrEmpty(name) && !name.equals(CUSTOM)) {
            return null;
        }

        return conditionSet;
    }

    public void load(TriggerMatcher matcher) {
        String conditionName = matcher.getConditionName();
        if (!StringUtils.isNullOrEmpty(conditionName)) {
            combobBox.setSelectedItem(conditionName);
        }

        conditionSet = matcher.getConditionSet();
        if (conditionSet != null) {
            combobBox.addItem(CUSTOM);
            combobBox.setSelectedItem(CUSTOM);
        }
    }
}
