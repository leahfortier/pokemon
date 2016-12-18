package battle.effect.status;

import battle.Battle;
import pokemon.ActivePokemon;
import util.StringUtils;

class NoStatus extends Status {
    private static final long serialVersionUID = 1L;

    NoStatus() {
        super(StatusCondition.NO_STATUS);
    }

    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return true;
    }

    public String getCastMessage(ActivePokemon p) {
        return StringUtils.empty();
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return StringUtils.empty();
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return StringUtils.empty();
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }
}
