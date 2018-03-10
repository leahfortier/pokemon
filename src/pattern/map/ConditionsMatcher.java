package pattern.map;

import main.Global;
import map.condition.ConditionSet;
import util.serialization.JsonMatcher;
import util.FileIO;
import util.FileName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionsMatcher implements JsonMatcher {
    private List<ConditionMatcher> conditions = new ArrayList<>();

    private static ConditionsMatcher readConditions() {
        return JsonMatcher.fromFile(FileName.CONDITIONS, ConditionsMatcher.class);
    }

    public static List<ConditionMatcher> getConditionMatchers() {
        return readConditions().conditions;
    }

    public static Map<String, ConditionSet> getConditions() {
        ConditionsMatcher matcher = readConditions();

        Map<String, ConditionSet> map = new HashMap<>();
        for (ConditionMatcher condition : matcher.conditions) {
            if (map.containsKey(condition.getName())) {
                Global.error("Duplicate condition name: " + condition.getName());
            }

            map.put(condition.getName(), condition.getCondition());
        }

        return map;
    }

    public static void addCondition(ConditionMatcher condition) {
        ConditionsMatcher matcher = readConditions();
        matcher.conditions.add(condition);

        FileIO.overwriteFile(FileName.CONDITIONS, matcher.getJson());
    }
}
