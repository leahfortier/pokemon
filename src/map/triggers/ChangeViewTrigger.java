package map.triggers;

import gui.view.ViewMode;
import main.Game;
import message.MessageUpdate;
import message.Messages;

public class ChangeViewTrigger extends Trigger {
    private final ViewMode view;

    public ChangeViewTrigger(ViewMode viewMode) {
        this.view = viewMode;
    }

    @Override
    public void execute() {
        Game.instance().setViewMode(this.view);
    }

    public static void addChangeViewTriggerMessage(ViewMode viewMode) {
        Messages.add(new MessageUpdate().withTrigger(new ChangeViewTrigger(viewMode)));
    }
}
