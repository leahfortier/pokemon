package main;

import battle.Battle;
import gui.GameData;
import gui.view.BagView;
import gui.view.BattleView;
import gui.view.EvolutionView;
import gui.view.MainMenuView;
import gui.view.MapView;
import gui.view.MartView;
import gui.view.OptionsView;
import gui.view.PCView;
import gui.view.PartyView;
import gui.view.PokedexView;
import gui.view.StartView;
import gui.view.TrainerCardView;
import gui.view.View;
import item.Item;
import item.hold.HoldItem;
import message.Messages;
import namesies.ItemNamesies;
import namesies.PokemonNamesies;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import util.InputControl;
import util.Save;

import java.awt.Graphics;
import java.util.EnumMap;
import java.util.Map;

public class Game {
	private static Game game;
	public static void create() {
		game = new Game();
	}

	public static GameData getData() {
		return game.data;
	}

	public static CharacterData getPlayer() {
		return game.characterData;
	}

	public enum ViewMode {
		BAG_VIEW,
		BATTLE_VIEW,
		EVOLUTION_VIEW,
		MAIN_MENU_VIEW,
		MAP_VIEW,
		MART_VIEW,
		OPTIONS_VIEW,
		PARTY_VIEW,
		PC_VIEW,
		POKEDEX_VIEW,
		START_VIEW,
		TRAINER_CARD_VIEW,
	}
	
	private final GameData data;
	private final Map<ViewMode, View> viewMap;
	
	private CharacterData characterData;
	private ViewMode currentViewMode;
	private View currentView;
	
	private Game() {
		data = new GameData();
		
		viewMap = new EnumMap<>(ViewMode.class);
		viewMap.put(ViewMode.MAIN_MENU_VIEW, new MainMenuView());
		
		currentViewMode = ViewMode.MAIN_MENU_VIEW;
		currentView = viewMap.get(ViewMode.MAIN_MENU_VIEW);
	}
	
	private void setupCharacter() {
		characterData.addPokemon(null, new ActivePokemon(PokemonNamesies.EEVEE, 1, false, true));
		characterData.front().giveItem((HoldItem)Item.getItem(ItemNamesies.ORAN_BERRY));
	}
	
	private void checkViewSwitch(InputControl input) {
		if (!currentView.getViewModel().equals(currentViewMode)) {
			input.resetKeys();
			currentView = viewMap.get(currentViewMode);
			currentView.movedToFront();
		}
	}

	public static void update(int dt, InputControl input) {
		game.checkViewSwitch(input);
		game.currentView.update(dt, input);
		game.checkViewSwitch(input);
	}

	public static void setBattleViews(final Battle battle, final boolean seenWildPokemon) {
		((BattleView)game.viewMap.get((ViewMode.BATTLE_VIEW))).setBattle(battle);
		((MapView)game.viewMap.get(ViewMode.MAP_VIEW)).setBattle(battle, seenWildPokemon);
	}

	public static void setMapViewDialogue(final String dialogueName) {
		Messages.addMessage(game.data.getDialogue(dialogueName));
	}

	public static void draw(Graphics g) {
		game.currentView.draw(g);
	}
	
	public static void setViewMode(ViewMode mode) {
		game.currentViewMode = mode;
	}

	public static boolean isCurrentViewMode(ViewMode viewMode) {
		return game.currentViewMode == viewMode;
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
		game.data.getMap(startingMap).setCharacterToEntrance(game.characterData, startingMapEntrance);

		game.characterData.setFileNum(index);
		game.setupCharacter();
		game.setViews();
	}

	// TODO: Should have an interface to handle this inside of the ViewMode enum
	private void setViews() {
		viewMap.put(ViewMode.MAP_VIEW, new MapView());
		viewMap.put(ViewMode.BAG_VIEW, new BagView());
		viewMap.put(ViewMode.PARTY_VIEW, new PartyView());
		viewMap.put(ViewMode.PC_VIEW, new PCView());
		viewMap.put(ViewMode.POKEDEX_VIEW, new PokedexView());
		viewMap.put(ViewMode.MART_VIEW, new MartView());
		viewMap.put(ViewMode.TRAINER_CARD_VIEW, new TrainerCardView());
		viewMap.put(ViewMode.BATTLE_VIEW, new BattleView());
		viewMap.put(ViewMode.OPTIONS_VIEW, new OptionsView());
		viewMap.put(ViewMode.START_VIEW, new StartView());
		viewMap.put(ViewMode.EVOLUTION_VIEW, new EvolutionView());
	}
}
