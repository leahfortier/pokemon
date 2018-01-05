package map.triggers;

import gui.view.ViewMode;
import main.Game;
import message.MessageUpdate;
import message.Messages;

class ChangeViewTrigger extends Trigger {
    private final ViewMode view;
    
    ChangeViewTrigger(String viewMode, String condition) {
        super(TriggerType.CHANGE_VIEW, viewMode, condition);
        
        this.view = ViewMode.valueOf(viewMode);
    }
    
    protected void executeTrigger() {
        Game.instance().setViewMode(this.view);
    }
    
    public static void addChangeViewTriggerMessage(ViewMode viewMode) {
        Messages.add(new MessageUpdate().withTrigger(TriggerType.CHANGE_VIEW.createTrigger(viewMode.name()).getName()));
    }
}
