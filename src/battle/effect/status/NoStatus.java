package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import util.string.StringUtils;

class NoStatus extends Status {
    private static final long serialVersionUID = 1L;

    NoStatus() {
        super(StatusNamesies.NO_STATUS);
    }

    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return victim.getHP() > 0;
    }

    @Override
    public String getGenericCastMessage(ActivePokemon p) {
        return StringUtils.empty();
    }

    @Override
    public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }

    @Override
    public String getGenericRemoveMessage(ActivePokemon victim) {
        return StringUtils.empty();
    }

    @Override
    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return StringUtils.empty();
    }
}
