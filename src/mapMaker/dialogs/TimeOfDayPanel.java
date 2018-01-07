package mapMaker.dialogs;

import map.condition.ConditionKey;
import map.daynight.DayCycle;
import util.GUIUtils;
import util.StringAppender;
import util.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class TimeOfDayPanel extends JPanel {
    private final JCheckBox allCheckBox;
    private final JCheckBox[] timeCheckBoxes;

    public TimeOfDayPanel() {
        this.allCheckBox = GUIUtils.createCheckBox("All");

        DayCycle[] dayCycles = DayCycle.values();
        this.timeCheckBoxes = new JCheckBox[dayCycles.length];
        for (int i = 0; i < dayCycles.length; i++) {
            this.timeCheckBoxes[i] = GUIUtils.createCheckBox(StringUtils.properCase(dayCycles[i].name()));
        }

        this.allCheckBox.addActionListener(event -> {
            for (JCheckBox timeCheckBox : this.timeCheckBoxes) {
                timeCheckBox.setEnabled(!this.allCheckBox.isSelected());
            }
        });

        JComponent timeCheckBoxesPanel = GUIUtils.createHorizontalLayoutComponent((JComponent[])this.timeCheckBoxes);
        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createLabel("Time of Day:"),
                this.allCheckBox,
                timeCheckBoxesPanel
        );
    }

    public String getCondition() {
        if (this.allCheckBox.isSelected()) {
            return StringUtils.empty();
        }

        StringAppender condition = new StringAppender();
        for (int i = 0; i < timeCheckBoxes.length; i++) {
            if (timeCheckBoxes[i].isSelected()) {
                condition.appendDelimiter("|", ConditionKey.TIME_OF_DAY.getConditionString(DayCycle.values()[i].name()));
            }
        }

        return condition.toString();
    }
}
