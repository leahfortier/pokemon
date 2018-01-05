package pattern;

import pattern.generic.TriggerMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupTriggerMatcher extends TriggerMatcher {
    private String[] triggers;
    
    private String suffix;
    private List<String> globals;
    
    public GroupTriggerMatcher(final String suffix, final String... triggers) {
        this.triggers = triggers;
        this.suffix = suffix;
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
