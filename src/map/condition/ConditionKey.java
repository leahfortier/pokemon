package map.condition;

import main.Game;
import main.Global;
import map.daynight.DayCycle;
import trainer.player.Badge;
import util.StringUtils;
import util.TimeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ConditionKey {
    BADGE("badge",
            value -> Game.getPlayer().hasBadge(Badge.valueOf(value))
    ),
    TIME_OF_DAY("time_of_day",
            value -> DayCycle.getTimeOfDay() == DayCycle.valueOf(value)
    ),
    HOUR_OF_DAY("hour_of_day", value -> {
        int index = value.indexOf('-');
        int startHour = Integer.parseInt(value.substring(0, index));
        int endHour = Integer.parseInt(value.substring(index + 1));
        return TimeUtils.currentHourWithinInterval(startHour, endHour);
    }),
    NPC_INTERACTION("npc_interaction", value -> {
        int index = value.indexOf('$');
        String npcEntityName = value.substring(0, index);
        String interactionName = value.substring(index + 1);

        System.out.println(npcEntityName + " " + interactionName);

        if (!StringUtils.isNullOrEmpty(npcEntityName) && npcEntityName.startsWith("#")) {
            return !Game.getPlayer().isNpcInteraction(npcEntityName.substring(1), interactionName);
        }
        else {
            return Game.getPlayer().isNpcInteraction(npcEntityName, interactionName);
        }

    });

    private static final Pattern keyValuePattern = Pattern.compile(":([^:]+):([^:]+):");

    private final String keyName;
    private final ConditionParser conditionParser;

    ConditionKey(String keyName, ConditionParser conditionParser) {
        this.keyName = keyName;
        this.conditionParser = conditionParser;
    }

    public String getConditionString(String value) {
        return String.format(":%s:%s:", this.keyName, value);
    }

    private interface ConditionParser {
        boolean parseCondition(String value);
    }

    static boolean matches(String keyValueString) {
        return keyValuePattern.matcher(keyValueString).matches();
    }

    static boolean getConditionValue(String keyValueString) {
        Matcher matcher = keyValuePattern.matcher(keyValueString);
        if (!matcher.matches()) {
            Global.error(keyValueString + " does not match key value pattern. Should call matches method first.");
        }

        String key = matcher.group(1);
        String value = matcher.group(2);

        for (ConditionKey conditionKey : values()) {
            if (conditionKey.keyName.equals(key)) {
                return conditionKey.conditionParser.parseCondition(value);
            }
        }

        Global.error("No condition key with key name " + key + " found.");
        return false;
    }
}
