package gui.panel;

import gui.Button;
import gui.ButtonHoverAction;

import java.awt.Color;

public class ButtonPanel extends DrawPanel {

    public ButtonPanel(int x, int y, int panelWidth, int panelHeight) {
        super(x, y, panelWidth, panelHeight);
    }

    public ButtonPanel withBorderColor(Color borderColor) {
        return (ButtonPanel)super.withBorderColor(borderColor);
    }

    public ButtonPanel withBorderPercentage(int borderPercentage) {
        return (ButtonPanel)super.withBorderPercentage(borderPercentage);
    }

    public Button[] getButtons(int buttonWidth, int buttonHeight, int numRows, int numCols) {
        int borderSize = this.getBorderSize();

        int horizontalSpacing = this.width - 2*borderSize - numCols*buttonWidth;
        int verticalSpacing = this.height - 2*borderSize - numRows*buttonHeight;

        int xSpacing = horizontalSpacing / (numCols + 1);
        int ySpacing = verticalSpacing / (numRows + 1);

        Button[] buttons = new Button[numRows*numCols];
        for (int row = 0, index = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++, index++) {
                buttons[index] = new Button(
                        this.x + borderSize + xSpacing *(col + 1) + buttonWidth*col,
                        this.y + borderSize + ySpacing *(row + 1) + buttonHeight*row,
                        buttonWidth,
                        buttonHeight,
                        ButtonHoverAction.BOX,
                        Button.getBasicTransitions(index, numRows, numCols)
                );
            }
        }

        return buttons;
    }
}
