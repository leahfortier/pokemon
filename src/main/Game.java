package main;

import battle.Battle;
import gui.GameData;
import gui.view.View;
import gui.view.ViewMode;
import gui.view.battle.BattleView;
import gui.view.map.MapView;
import input.InputControl;
import item.ItemNamesies;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.CharacterData;
import util.Save;

import java.awt.Graphics;
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

	public static CharacterData getPlayer() {
		return instance().characterData;
	}

	private GameData data;
	private final Map<ViewMode, View> viewMap;
	
	private CharacterData characterData;
	private ViewMode currentViewMode;
	private View currentView;
	
	protected Game() {
		viewMap = new EnumMap<>(ViewMode.class);
		addView(ViewMode.MAIN_MENU_VIEW);
		
		currentViewMode = ViewMode.MAIN_MENU_VIEW;
		currentView = viewMap.get(ViewMode.MAIN_MENU_VIEW);
	}
	
	private void setupCharacter() {
		characterData.addPokemon(new ActivePokemon(PokemonNamesies.EEVEE, 1, false, true));
		characterData.front().giveItem(ItemNamesies.ORAN_BERRY);
	}
	
	private void checkViewSwitch() {
		if (!currentView.getViewModel().equals(currentViewMode)) {
			InputControl.instance().resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront();
		}
	}

	public void update(int dt) {
		checkViewSwitch();
		currentView.update(dt);
		checkViewSwitch();
	}

	public void setBattleViews(final Battle battle, final boolean seenWildPokemon) {
		((BattleView)viewMap.get((ViewMode.BATTLE_VIEW))).setBattle(battle);
		((MapView)viewMap.get(ViewMode.MAP_VIEW)).setBattle(battle, seenWildPokemon);
	}

	public void draw(Graphics g) {
		currentView.draw(g);
	}
	
	public void setViewMode(ViewMode mode) {
		currentViewMode = mode;
	}

	public void loadSave(int index) {
		characterData = Save.load(index);
		setViews();
	}
	
	public void newSave(int index) {
		characterData = new CharacterData();
		
		String startingMap = "PlayersHouseUp";
		String startingMapEntrance = "GameStartLocation";
		characterData.setMap(startingMap, startingMapEntrance);
		data.getMap(startingMap).setCharacterToEntrance();

		characterData.setFileNum(index);
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

	protected void setCharacterData(CharacterData characterData) {
		this.characterData = characterData;
	}
}
