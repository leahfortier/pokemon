package draw.layout;

import draw.button.Button;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;

import java.awt.Color;

public class QuestionLayout {
    private final Button yesButton;
    private final Button noButton;

    public QuestionLayout(int yesIndex, int noIndex, ButtonPressAction yesAction, ButtonPressAction noAction) {
        ButtonLayout questionLayout = BasicPanels.getFullMessagePanelLayout(2, 4, 8)
                                                 .withButtonSetup(panel -> panel.skipInactive()
                                                                                .withTransparentCount(2)
                                                                                .withBorderPercentage(15)
                                                                                .withLabelSize(30)
                                                                                .withBlackOutline());
        // Bottom middle left
        yesButton = questionLayout.getButton(
                1, 1,
                new ButtonTransitions().left(noIndex).right(noIndex),
                yesAction
        ).setup(panel -> panel.withBackgroundColor(new Color(120, 200, 80)).withLabel("Yes"));

        // Bottom middle right
        noButton = questionLayout.getButton(
                1, 2,
                new ButtonTransitions().left(yesIndex).right(yesIndex),
                noAction
        ).setup(panel -> panel.withBackgroundColor(new Color(220, 20, 20)).withLabel("No"));
    }

    public Button getYesButton() {
        return this.yesButton;
    }

    public Button getNoButton() {
        return this.noButton;
    }
}
