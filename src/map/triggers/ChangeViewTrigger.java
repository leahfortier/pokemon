package map.triggers;

import gui.view.ViewMode;
import main.Game;
import message.MessageUpdate;
import message.Messages;

public class ChangeViewTrigger extends Trigger {
    private final ViewMode view;

    public ChangeViewTrigger(ViewMode viewMode) {
        super(viewMode.name());
        this.view = viewMode;
    }

    @Override
    protected void executeTrigger() {
        Game.instance().setViewMode(this.view);
    }

    public static void addChangeViewTriggerMessage(ViewMode viewMode) {
        Messages.add(new MessageUpdate().withTrigger(new ChangeViewTrigger(viewMode)));
    }
}
