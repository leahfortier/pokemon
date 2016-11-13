package pattern;

public class GroupTriggerMatcher extends EntityMatcher {
    public String[] triggers;

    public String suffix;
    public String[] globals;

    public GroupTriggerMatcher(final String... triggers) {
        this.triggers = triggers;
    }
}
