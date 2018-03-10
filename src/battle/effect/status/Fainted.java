package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import util.string.StringUtils;

class Fainted extends Status {
    private static final long serialVersionUID = 1L;

    public Fainted() {
        super(StatusNamesies.FAINTED);
    }

    // Fainted status condition applies regardless of other status conditions
    @Override
    protected boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return this.statusApplies(b, caster, victim);
    }

    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return victim.getHP() == 0;
    }

    @Override
    public String getGenericCastMessage(ActivePokemon p) {
        return p.getName() + " fainted!";
    }

    @Override
    public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
        return sourcerer.getName() + "'s " + sourceName + " caused " + victim.getName() + " to faint!";
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
