package trainer;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.InvokeInterfaces.SwitchOutEffect;
import item.bag.Bag;
import main.Global;
import pokemon.active.PartyPokemon;
import util.RandomUtils;
import util.serialization.Serializable;

import java.util.ArrayList;
import java.util.List;

public abstract class Trainer implements Team, Serializable {
    private static final long serialVersionUID = -7797121866082399148L;

    public static final int MAX_POKEMON = 6;

    protected String name;
    protected List<PartyPokemon> team;
    private int cashMoney;

    private TrainerAction action;
    private TeamEffectList effects;
    private Bag bag;

    private int frontIndex;
    private int switchIndex;

    private transient Battle battle;

//    protected boolean isBeTryingToSwitchRunOrUseItem;
//    protected boolean isBTTSROUI;

    public Trainer(String name, int cashMoney) {
        this.name = name;
        this.cashMoney = cashMoney;

        team = new ArrayList<>();
        effects = new TeamEffectList();
        frontIndex = 0;

        bag = new Bag();
    }

    public abstract void addPokemon(PartyPokemon p);

    @Override
    public ActivePokemon front() {
        if (!inBattle()) {
            this.setFront();
        }

        return (ActivePokemon)team.get(frontIndex);
    }

    @Override
    public int getTeamIndex(ActivePokemon teamMember) {
        for (int i = 0; i < team.size(); i++) {
            if (teamMember == team.get(i)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public List<PartyPokemon> getTeam() {
        return team;
    }

    @Override
    public TeamEffectList getEffects() {
        return effects;
    }

    public String getName() {
        return name;
    }

    protected int getFrontIndex() {
        return this.frontIndex;
    }

    protected void setFront(int index) {
        if (frontIndex == index) {
            return;
        }

        // Apply any effects that take place when switching out
        if (this.inBattle()) {
            SwitchOutEffect.invokeSwitchOutEffect(front());
        }

        frontIndex = index;
    }

    // Sets the front Pokemon to be the first Pokemon capable of battling
    protected void setFront() {
        for (int i = 0; i < team.size(); i++) {
            if (team.get(i).canFight()) {
                setFront(i);
                return;
            }
        }

        Global.error("None of your Pokemon are capable of battling!");
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }

    // Returns the battle the trainer is currently in or null if not in battle
    public Battle getBattle() {
        return this.battle;
    }

    public boolean inBattle() {
        return this.battle != null;
    }

    // Should be called when the trainer enters a new battle -- Sets all Pokemon to not be used yet and sets the front Pokemon
    @Override
    public void enterBattle(Battle b) {
        this.setBattle(b);
        team.forEach(PartyPokemon::resetAttributes);
        this.setAction(TrainerAction.FIGHT);
        this.setFront();
        this.getEffects().reset();
    }

    public void exitBattle() {
        this.battle = null;
    }

    @Override
    public void resetUsed() {
        for (PartyPokemon p : team) {
            p.setUsed(false);
        }
    }

    public Bag getBag() {
        return bag;
    }

    public int getDatCashMoney() {
        return cashMoney;
    }

    // Money in da bank
    public void getDatCashMoney(int datCash) {
        cashMoney += datCash;
    }

    // I don't know why I can't take this seriously, sorry guys
    public int sucksToSuck(int datCash) {
        int prev = cashMoney;
        cashMoney = Math.max(0, cashMoney - datCash);
        return prev - cashMoney;
    }

    @Override
    public boolean blackout(Battle b) {
        boolean maxUsed = usedMaxPokemon(b);
        for (PartyPokemon p : team) {
            if (p.canFight() && (!maxUsed || p.isBattleUsed())) {
                return false;
            }
        }

        return true;
    }

    public void healAll() {
        team.forEach(PartyPokemon::fullyHeal);
    }

    public void switchToRandom(Battle b) {
        boolean maxUsed = usedMaxPokemon(b);
        List<Integer> valid = new ArrayList<>();
        for (int i = 0; i < team.size(); i++) {
            PartyPokemon p = team.get(i);
            if (i == frontIndex || !p.canFight() || (maxUsed && !p.isBattleUsed())) {
                continue;
            }

            valid.add(i);
        }

        if (valid.size() == 0) {
            Global.error("You shouldn't be switching when you have nothing to switch to!");
        }

        setFront(RandomUtils.getRandomValue(valid));
    }

    public boolean canSwitch(Battle b, int switchIndex) {

        // This Pokemon is already out!!
        if (switchIndex == frontIndex) {
            return false;
        }

        ActivePokemon curr = front();
        PartyPokemon toSwitch = team.get(switchIndex);

        // Cannot switch to a fainted Pokemon
        if (!toSwitch.canFight()) {
            return false;
        }

        // Cannot switch to an unused Pokemon if you have already used the maximum number of Pokemon
        if (usedMaxPokemon(b) && !toSwitch.isBattleUsed()) {
            return false;
        }

        // If current Pokemon is dead then you must switch!
        if (curr.isFainted(b)) {
            return true;
        }

        // Front Pokemon is alive -- Check if they are able to switch out, if not, display the appropriate message
        return curr.canEscape(b);
    }

    public void performSwitch(Battle b) {
        this.setFront(this.switchIndex);
        b.enterBattle(this.front());
    }

    public void setSwitchIndex(int switchIndex) {
        this.switchIndex = switchIndex;
    }

    public boolean usedMaxPokemon(Battle b) {
        return numPokemonUsed() == b.getOpponent().maxPokemonAllowed();
    }

    private int numPokemonUsed() {
        return (int)team.stream().filter(PartyPokemon::isBattleUsed).count();
    }

    // Returns true if the trainer has Pokemon (other than the one that is currently fighting) that is able to fight
    public boolean hasRemainingPokemon(Battle b) {
        boolean maxUsed = usedMaxPokemon(b);
        for (int i = 0; i < team.size(); i++) {
            if (i == frontIndex) {
                continue;
            }

            if (team.get(i).canFight() && (!maxUsed || team.get(i).isBattleUsed())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TrainerAction getAction() {
        return action;
    }

    public void setAction(TrainerAction a) {
        action = a;
    }

    public void swapPokemon(int i, int j) {
        PartyPokemon tmp = team.get(i);
        team.set(i, team.get(j));
        team.set(j, tmp);
    }

    public void replaceFront(ActivePokemon newFront) {
        this.team.set(this.frontIndex, newFront);
    }
}
