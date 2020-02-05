package battle.stages;

import battle.ActivePokemon;
import battle.effect.source.CastSource;
import main.Global;
import message.Messages;
import util.serialization.Serializable;

public interface ModifyStageMessenger extends Serializable {
    String getMessage(String victimName, String possessiveVictim, String statName, String changed);

    // Creates and adds the actual modify message to the queue
    default void addMessage(ActivePokemon caster, ActivePokemon victim, CastSource source, int val, String statName) {
        this.set(caster, source);

        String change = getChangedStatString(val);
        String possessiveVictim = caster == victim ? "its" : victim.getName() + "'s";
        String message = this.getMessage(victim.getName(), possessiveVictim, statName, change);

        Messages.add(message);
    }

    // Called before getting the message if the messenger needs to set any appropriate values first
    default void set(ActivePokemon caster, CastSource source) {}

    // -3 or lower: drastically lowered
    // -2: sharply lowered
    // -1: lowered
    //  0: <throws error>
    //  1: raised
    //  2: sharply raised
    //  3 or higher: drastically raised
    static String getChangedStatString(int val) {
        if (val == 0) {
            Global.error("Cannot modify a stage by zero.");
        }

        int positive = Math.abs(val);
        String modifier;
        if (positive == 1) {
            modifier = "";
        } else if (positive == 2) {
            modifier = "sharply ";
        } else {
            modifier = "drastically ";
        }

        String direction;
        if (val > 0) {
            direction = "raised";
        } else {
            direction = "lowered";
        }

        return modifier + direction;
    }

    // For effects that don't have a unique message (most effects will use this)
    class DefaultModifyStageMessenger implements ModifyStageMessenger {
        private static final long serialVersionUID = 1L;

        private CastSource source;
        private String casterSourcePossessive;

        @Override
        public void set(ActivePokemon caster, CastSource source) {
            this.source = source;
            if (source.hasSourceName()) {
                // Ex: Gyarados's Intimidate, Bulbasaur's Absorb Bulb
                this.casterSourcePossessive = caster.getName() + "'s " + source.getSourceName(caster);
            }
        }

        @Override
        public String getMessage(String victimName, String possessiveVictim, String statName, String changed) {
            switch (source) {
                case ATTACK:
                case USE_ITEM:
                    // Bulbasaur's Attack was sharply raised!
                    return String.format("%s's %s was %s!", victimName, statName, changed);
                case ABILITY:
                case HELD_ITEM:
                    // Gyarados's Intimidate lowered Charmander's Attack!
                    // Bulbasaur's Absorb Bulb raised its Special Attack!
                    return String.format("%s %s %s %s!", casterSourcePossessive, changed, possessiveVictim, statName);
                case EFFECT:
                    Global.error("Effect message should be handled manually using the other modifyStage method.");
                    break;
                default:
                    Global.error("Unknown source for stage modifier.");
                    break;
            }

            return "";
        }
    }
}
