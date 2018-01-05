package mapMaker.dialogs.action.trigger;

import main.Global;
import util.StringUtils;

import javax.swing.JPanel;

abstract class TriggerContentsPanel extends JPanel {
    protected abstract void load(String triggerContents);
    protected abstract String getTriggerContents();
    
    static class EmptyTriggerContentsPanel extends TriggerContentsPanel {
    
        @Override
        protected void load(String triggerContents) {
            if (!StringUtils.isNullOrEmpty(triggerContents)) {
                Global.error("Trigger contents should be empty: " + triggerContents);
            }
        }
        
        @Override
        protected String getTriggerContents() {
            return StringUtils.empty();
        }
    }
}
