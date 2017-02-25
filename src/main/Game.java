package main;

import battle.Battle;
import gui.GameData;
import gui.view.View;
import gui.view.ViewMode;
import gui.view.battle.BattleView;
import gui.view.map.MapView;
import input.InputControl;
import item.ItemNamesies;
import map.MapName;
import map.overworld.OverworldTool;
import message.Messages;
import pattern.map.MapTransitionMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.Player;
import util.Save;

import java.awt.Graphics;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;

public class Game {
	private static Game instance;
	public static Game instance() {
		if (instance == null) {
			instance = new Game();
			instance.data = new GameData();
			instance.data.loadData();
		}

		return instance;
	}

	public static GameData getData() {
		return instance().data;
	}

	public static Player getPlayer() {
		return instance().player;
	}

	private GameData data;
	private final Map<ViewMode, View> viewMap;

	private Player player;
	private ViewMode currentViewMode;
	private ArrayDeque<View> currentView;

	protected Game() {
		viewMap = new EnumMap<>(ViewMode.class);
		addView(ViewMode.MAIN_MENU_VIEW);

		currentView = new ArrayDeque<>();
		currentView.push(viewMap.get(ViewMode.MAIN_MENU_VIEW));

		currentViewMode = ViewMode.MAIN_MENU_VIEW;
	}
	
	private void setupCharacter() {
		player.addPokemon(new ActivePokemon(PokemonNamesies.EEVEE, 1, false, true));
		player.front().giveItem(ItemNamesies.ORAN_BERRY);
	}
	
	private void checkViewSwitch() {
		if (!currentView.peek().getViewModel().equals(currentViewMode)) {
			InputControl.instance().resetKeys();

			if (this.hasViewModeInStack(currentViewMode)) {
				while (currentView.peek().getViewModel() != currentViewMode) {
					currentView.pop();
				}
			} else {
				currentView.push(viewMap.get(currentViewMode));
			}

			currentView.peek().movedToFront();
		}
	}

	private boolean hasViewModeInStack(ViewMode viewMode) {
		for (View view : currentView) {
			if (view.getViewModel() == viewMode) {
				return true;
			}
		}

		return false;
	}

	public void update(int dt) {
		checkViewSwitch();
		currentView.peek().update(dt);
		checkViewSwitch();
	}

	public void setBattleViews(final Battle battle, final boolean seenWildPokemon) {
		((BattleView)viewMap.get((ViewMode.BATTLE_VIEW))).setBattle(battle);
		((MapView)viewMap.get(ViewMode.MAP_VIEW)).setBattle(battle, seenWildPokemon);
	}

	public void draw(Graphics g) {
		currentView.peek().draw(g);
	}
	
	public void setViewMode(ViewMode mode) {
		currentViewMode = mode;
	}

	public void popView() {
		currentView.pop();
		currentViewMode = currentView.peek().getViewModel();
	}

	public void loadSave(int index) {
		player = Save.load(index);
		setViews();
	}
	
	public void newSave(int index) {
		player = new Player();
		
		MapName startingMap = new MapName("Depth First Search Town", "PlayersHouseUp");
		MapTransitionMatcher startTransition = this.data.getMap(startingMap).getEntrance("startTransition");
		player.setMap(startTransition);
		data.getMap(startingMap).setCharacterToEntrance();

		player.setFileNum(index);
		setupCharacter();
		setViews();

		Messages.clearAllMessages();
	}

	private void addView(ViewMode viewMode) {
		viewMap.put(viewMode, viewMode.createView());
	}

	private void setViews() {
		for (ViewMode viewMode : ViewMode.values()) {
			if (viewMode == ViewMode.MAIN_MENU_VIEW) {
				continue;
			}

			addView(viewMode);
		}
	}

	protected static void newInstance(Game newGame) {
		instance = newGame;
	}

	protected void setPlayer(Player player) {
		this.player = player;
	}

	protected void setGameData(GameData data) {
		this.data = data;
	}
}
