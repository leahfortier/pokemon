package pattern;

import map.triggers.Trigger;
import pattern.generic.TriggerMatcher;

import java.util.Arrays;
import java.util.List;

public class GroupTriggerMatcher extends TriggerMatcher {
    private String suffix;
    private Trigger[] triggers;

    public GroupTriggerMatcher(final String suffix, final List<Trigger> triggers) {
        this(suffix, triggers.toArray(new Trigger[0]));
    }

    public GroupTriggerMatcher(final String suffix, final Trigger... triggers) {
        this.suffix = suffix;
        this.triggers = triggers;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public List<Trigger> getTriggers() {
        return Arrays.asList(triggers);
    }
}
