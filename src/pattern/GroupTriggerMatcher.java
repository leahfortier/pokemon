package pattern;

import map.triggers.Trigger;
import pattern.generic.TriggerMatcher;

import java.util.Arrays;
import java.util.List;

public class GroupTriggerMatcher extends TriggerMatcher {
    private String suffix;
    private String[] triggers;

    public GroupTriggerMatcher(final String suffix, final List<Trigger> triggers) {
        this(suffix, triggers.toArray(new Trigger[0]));
    }

    public GroupTriggerMatcher(final String suffix, final Trigger... triggers) {
        this.suffix = suffix;
        this.triggers = new String[triggers.length];
        for (int i = 0; i < triggers.length; i++) {
            this.triggers[i] = triggers[i].getName();
            triggers[i].addData();
        }
    }

    public String getSuffix() {
        return this.suffix;
    }

    public List<String> getTriggers() {
        return Arrays.asList(triggers);
    }
}
