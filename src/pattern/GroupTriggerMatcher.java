package pattern;

import map.triggers.Trigger;
import pattern.generic.TriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GroupTriggerMatcher extends TriggerMatcher {
    private String[] triggers;

    private String suffix;
    private List<String> globals;

    public GroupTriggerMatcher(final String suffix, final Trigger... triggers) {
        this.suffix = suffix;
        this.triggers = Arrays.stream(triggers)
                              .map(Trigger::getName)
                              .collect(Collectors.toList())
                              .toArray(new String[0]);
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
