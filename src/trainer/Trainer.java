package trainer;

import battle.Battle;
import battle.effect.SwitchOutEffect;
import battle.effect.generic.Effect;
import battle.effect.generic.TeamEffect;
import battle.effect.status.StatusCondition;
import item.bag.Bag;
import item.Item;
import main.Global;
import battle.effect.generic.EffectNamesies;
import pokemon.ActivePokemon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Trainer implements Team, Serializable {
	private static final long serialVersionUID = -7797121866082399148L;

	public enum Action implements Serializable {
		FIGHT(0),
		SWITCH(6),
		ITEM(6),
		RUN(6);
		
		private int priority;
		
		Action(int p) {
			priority = p;
		}
		
		public int getPriority() {
			return priority;
		}
	}
	
	public static final int MAX_POKEMON = 6;
	
	protected List<ActivePokemon> team;
	protected List<TeamEffect> effects;
	protected int frontIndex;
	protected String name;
	protected Action action;
	protected int cashMoney;
//	protected boolean isBeTryingToSwitchRunOrUseItem;
//	protected boolean isBTTSROUI;
	
	protected Bag bag;
	
	public Trainer(String name, int cashMoney) {
		team = new ArrayList<>();
		effects = new ArrayList<>();
		frontIndex = 0;
		this.name = name; 
		this.cashMoney = cashMoney;
		
		bag = new Bag();
	}
	
	public ActivePokemon front() {
		return team.get(frontIndex);
	}
	
	public List<ActivePokemon> getTeam() {
		return team;
	}
	
	public List<TeamEffect> getEffects() {
		return effects;
	}
	
	public void resetEffects() {
		effects = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setFront(int index) {
		if (frontIndex == index) {
			return;
		}
		
		// Apply any effects that take place when switching out
		if (front().getAbility() instanceof SwitchOutEffect) {
			((SwitchOutEffect)front().getAbility()).switchOut(front());
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
	
	// Should be called when the trainer enters a new battle -- Sets all Pokemon to not be used yet and sets the front Pokemon
	public void enterBattle() {
		for (ActivePokemon p : team) {
			p.resetAttributes();
		}

		setFront();
	}
	
	public void resetUsed() {
		for (int i = 0; i < team.size(); i++) {
			if (i == frontIndex) {
				team.get(i).getAttributes().setUsed(true);
			}
			else {
				team.get(i).getAttributes().setUsed(false);
			}
		}
	}
	
	public abstract void addPokemon(Battle b, ActivePokemon p);

	// TODO: Namesies
	public void addItem(Item i, int amt) {
		bag.addItem(i, amt);
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

	public boolean hasEffect(EffectNamesies effect) {
		return Effect.hasEffect(effects, effect);
	}
	
	public void addEffect(TeamEffect e) {
		effects.add(e.newInstance());
	}
	
	public boolean blackout() {
		for (ActivePokemon p : team) {
			if (p.canFight()) {
				return false;
			}
		}

		return true;
	}
	
	public void healAll() {
		for (ActivePokemon p : team) {
			p.fullyHeal();
		}
	}
	
	public void switchToRandom() {
		List<Integer> valid = new ArrayList<>();
		for (int i = 0; i < team.size(); i++) {
			if (i == frontIndex || !team.get(i).canFight()) {
				continue;
			}

			valid.add(i);
		}
		
		if (valid.size() == 0) {
			Global.error("You shouldn't be switching when you have nothing to switch to!");
		}

		setFront(Global.getRandomValue(valid));
	}
	
	public boolean canSwitch(Battle b, int switchIndex) {

		// This Pokemon is already out!!
		if (switchIndex == frontIndex) {
			return false;
		}
		
		ActivePokemon curr = front();
		ActivePokemon toSwitch = team.get(switchIndex);
		
		// Cannot switch to a fainted Pokemon, and if current Pokemon is dead then you must switch!
		if (!toSwitch.canFight()) {
			return false;
		}

		if (curr.hasStatus(StatusCondition.FAINTED)) {
			return true;
		}
		
		// Front Pokemon is alive -- Check if they are able to switch out, if not, display the appropriate message
		return curr.canEscape(b);
	}
	
	// Returns true if the trainer has Pokemon (other than the one that is currently fighting) that is able to fight
	public boolean hasRemainingPokemon() {
		for (int i = 0; i < team.size(); i++) {
			if (i == frontIndex) {
				continue;
			}
			
			if (team.get(i).canFight()) {
				return true;
			}
		}
		
		return false;
	}
	
	public void performAction(Battle b, Action a) {
		setAction(a);
		b.fight();
	}
	
	public void setAction(Action a) {
		action = a;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void swapPokemon(int i, int j) {
		ActivePokemon tmp = team.get(i);
		team.set(i, team.get(j));
		team.set(j, tmp);
	}
}
