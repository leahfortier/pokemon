package pattern;

import map.triggers.Trigger;
import pattern.generic.TriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupTriggerMatcher extends TriggerMatcher {
    private String[] triggers;

    private String suffix;
    private List<String> globals;

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
        return new ArrayList<>(Arrays.asList(triggers));
    }

    public List<String> getGlobals() {
        List<String> globals = new ArrayList<>();
        if (this.globals != null) {
            globals.addAll(this.globals);
        }

        return globals;
    }

    public void addGlobals(String... globalNames) {
        if (this.globals == null) {
            this.globals = new ArrayList<>();
        }

        Collections.addAll(this.globals, globalNames);
    }
}
