package pattern;

import pattern.generic.TriggerMatcher;

public class GroupTriggerMatcher extends TriggerMatcher {
    public String[] triggers;

    public String suffix;
    public String[] globals;

    public GroupTriggerMatcher(final String... triggers) {
        this.triggers = triggers;
    }
}
