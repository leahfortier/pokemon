package map.triggers;

import gui.view.ViewMode;
import main.Game;
import map.condition.Condition;
import message.MessageUpdate;
import message.Messages;

public class ChangeViewTrigger extends Trigger {
    private final ViewMode view;

    ChangeViewTrigger(String viewMode, Condition condition) {
        this(ViewMode.valueOf(viewMode), condition);
    }

    public ChangeViewTrigger(ViewMode viewMode, Condition condition) {
        super(viewMode.name(), condition);
        this.view = viewMode;
    }

    @Override
    protected void executeTrigger() {
        Game.instance().setViewMode(this.view);
    }

    public static void addChangeViewTriggerMessage(ViewMode viewMode) {
        Messages.add(new MessageUpdate().withTrigger(new ChangeViewTrigger(viewMode, null).getName()));
    }
}
