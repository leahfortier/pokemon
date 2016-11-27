package main;

import battle.Battle;
import gui.GameData;
import gui.view.BattleView;
import gui.view.MapView;
import gui.view.View;
import gui.view.ViewMode;
import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.CharacterData;
import input.InputControl;
import util.Save;

import java.awt.Graphics;
import java.util.EnumMap;
import java.util.Map;

public class Game {
	private static Game game;
	public static void create() {
		game = new Game();
		game.data = new GameData();
		game.data.loadData();
	}

	public static GameData getData() {
		return game.data;
	}

	public static CharacterData getPlayer() {
		return game.characterData;
	}

	private GameData data;
	private final Map<ViewMode, View> viewMap;
	
	private CharacterData characterData;
	private ViewMode currentViewMode;
	private View currentView;
	
	private Game() {
		viewMap = new EnumMap<>(ViewMode.class);
		addView(ViewMode.MAIN_MENU_VIEW);
		
		currentViewMode = ViewMode.MAIN_MENU_VIEW;
		currentView = viewMap.get(ViewMode.MAIN_MENU_VIEW);
	}
	
	private void setupCharacter() {
		characterData.addPokemon(null, new ActivePokemon(PokemonNamesies.EEVEE, 1, false, true));
		characterData.front().giveItem(ItemNamesies.ORAN_BERRY);
	}
	
	private void checkViewSwitch() {
		if (!currentView.getViewModel().equals(currentViewMode)) {
			InputControl.instance().resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront();
		}
	}

	public static void update(int dt) {
		game.checkViewSwitch();
		game.currentView.update(dt);
		game.checkViewSwitch();
	}

	public static void setBattleViews(final Battle battle, final boolean seenWildPokemon) {
		((BattleView)game.viewMap.get((ViewMode.BATTLE_VIEW))).setBattle(battle);
		((MapView)game.viewMap.get(ViewMode.MAP_VIEW)).setBattle(battle, seenWildPokemon);
	}

	public static void draw(Graphics g) {
		game.currentView.draw(g);
	}
	
	public static void setViewMode(ViewMode mode) {
		game.currentViewMode = mode;
	}

	public static void loadSave(int index) {
		game.characterData = Save.load(index);
		game.setViews();
	}
	
	public static void newSave(int index) {
		game.characterData = new CharacterData();
		
		String startingMap = "PlayersHouseUp";
		String startingMapEntrance = "GameStartLocation";
		game.characterData.setMap(startingMap, startingMapEntrance);
		game.data.getMap(startingMap).setCharacterToEntrance(startingMapEntrance);

		game.characterData.setFileNum(index);
		game.setupCharacter();
		game.setViews();
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
}
